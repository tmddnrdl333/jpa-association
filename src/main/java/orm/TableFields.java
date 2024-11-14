package orm;


import orm.exception.InvalidEntityException;
import orm.meta.EntityFieldMeta;
import orm.settings.JpaSettings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TableField에 대한 일급객체
 */
public class TableFields<E> {

    // allFields 중 변경된 필드를 추적하기 위한 BitSet
    private final BitSet changedFieldsBitset;
    private final List<TableField> allFields;

    public TableFields (E entity, JpaSettings settings) {
        this.allFields = extractTableFieldsFromEntity(entity, settings);
        this.changedFieldsBitset = new BitSet(allFields.size());
    }

    public TableFields (E entity) {
        this.allFields = extractTableFieldsFromEntity(entity, JpaSettings.ofDefault());
        this.changedFieldsBitset = new BitSet(allFields.size());
    }

    public TableFields(List<TableField> allFields) {
        this.allFields = allFields;
        this.changedFieldsBitset = new BitSet(allFields.size());
    }

    public List<TableField> getAllFields() {
        return allFields;
    }

    public void setFieldChanged(int index, boolean changed) {
        changedFieldsBitset.set(index, changed);
    }

    // id를 제외한 모든 필드 추출 (연관관계 제외)
    public List<TableField> getNonIdFields() {
        return allFields.stream()
                .filter(field -> !field.isId())
                .toList();
    }

    // 변경된 필드 추출
    public List<TableField> getChangedFields() {
        List<TableField> allFields = this.allFields;
        List<TableField> result = new ArrayList<>(allFields.size());

        for (int i = 0; i < allFields.size(); i++) {
            if (this.changedFieldsBitset.get(i)) {
                result.add(allFields.get(i));
            }
        }
        return result;
    }

    // 모든 필드를 주어진 필드로 교체한다.
    public void replaceAllFields(List<? extends TableField> newTableFields) {
        Map<String, Object> fieldValueMap = newTableFields.stream()
                .filter(tableField -> tableField.getFieldValue() != null)
                .collect(Collectors.toMap(TableField::getFieldName, TableField::getFieldValue));

        for (TableField field : allFields) {
            Object fieldValue = fieldValueMap.get(field.getFieldName());
            field.setFieldValue(fieldValue);
        }
    }

    public int size() {
        return this.getAllFields().size();
    }

    // @Transient와 @Column이 동시에 존재하는 경우 금지
    private void throwIfContainsTransientColumn(EntityFieldMeta entityFieldMeta, Class<?> entityClass) {
        if (entityFieldMeta.hasConflictTransientColumn()) {
            throw new InvalidEntityException(String.format(
                    "class %s @Transient & @Column cannot be used in same field"
                    , entityClass.getName())
            );
        }
    }

    // 엔티티로부터 TableField 추출
    private List<TableField> extractTableFieldsFromEntity(E entity, JpaSettings settings) {
        Class<?> entityClass = entity.getClass();
        Field[] declaredFields = entityClass.getDeclaredFields();

        // 연관관계가 아닌 필드
        List<TableField> columnList = new ArrayList<>(declaredFields.length);

        for (Field declaredField : declaredFields) {
            var entityProperty = new EntityFieldMeta(declaredField);
            throwIfContainsTransientColumn(entityProperty, entityClass);

            // transient 필드 무시
            if (entityProperty.isTransientAnnotated()) {
                continue;
            }

            // 연관관계 필드 무시
            if (entityProperty.isRelationAnnotation()) {
                continue;
            }

            // @Id가 존재 여부에 따라 테이블 필드 생성
            var tableField = entityProperty.isIdAnnotated()
                    ? new TablePrimaryField(declaredField, entity, settings)
                    : new TableField(declaredField, entity, settings);

            columnList.add(tableField);
        }

        return columnList;
    }
}
