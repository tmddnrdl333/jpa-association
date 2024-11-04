package orm;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.dsl.holder.EntityIdHolder;
import orm.exception.EntityHasNoDefaultConstructorException;
import orm.exception.InvalidEntityException;
import orm.exception.InvalidIdMappingException;
import orm.settings.JpaSettings;
import orm.validator.EntityValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 엔티티 클래스로부터 테이블 정보를 추출한 클래스
 *
 * @param <E> @Entity 어노테이션이 붙은 클래스
 */
public class TableEntity<E> {

    private static final Logger logger = LoggerFactory.getLogger(TableEntity.class);

    private final String tableName;

    private final E entity;
    private final Class<E> tableClass;

    private final TablePrimaryField id;

    private final List<TableField> allFields;

    // allFields 중 변경된 필드를 추적하기 위한 BitSet
    private final BitSet changedFields;

    private final JpaSettings jpaSettings;

    public TableEntity(Class<E> entityClass, JpaSettings settings) {
        this.entity = createNewInstanceByDefaultConstructor(entityClass);
        new EntityValidator<>(entity).validate();

        this.id = extractId(settings);
        this.tableName = extractTableName(entityClass, settings);
        this.jpaSettings = settings;
        this.tableClass = entityClass;
        this.allFields = extractAllPersistenceFields(entityClass, settings);
        this.changedFields = new BitSet(allFields.size());
    }

    public TableEntity(E entity, JpaSettings settings) {
        this.entity = entity;
        new EntityValidator<>(entity).validate();

        Class<E> entityClass = (Class<E>) entity.getClass();
        this.id = extractId(settings);
        this.tableName = extractTableName(entityClass, settings);
        this.jpaSettings = settings;
        this.tableClass = entityClass;
        this.allFields = extractAllPersistenceFields(entityClass, settings);
        this.changedFields = new BitSet(allFields.size());
    }

    public TableEntity(Class<E> entityClass) {
        this(entityClass, JpaSettings.ofDefault());
    }

    public TableEntity(E entity) {
        this(entity, JpaSettings.ofDefault());
    }

    public JpaSettings getJpaSettings() {
        return jpaSettings;
    }

    public String getTableName() {
        return tableName;
    }

    public TablePrimaryField getId() {
        return id;
    }

    public void setIdValue(Object idValue) {
        id.setIdValue(idValue);
    }

    public void markFieldChanged(int index) {
        changedFields.set(index, true);
    }

    public boolean hasDbGeneratedId() {
        GenerationType idGenerationType = getIdGenerationType();
        return idGenerationType == GenerationType.IDENTITY;
    }

    // id(pk) 생성 전략
    public GenerationType getIdGenerationType() {
        GeneratedValue generatedValue = getId().getGeneratedValue();
        if (generatedValue == null) {
            return null;
        }
        return generatedValue.strategy();
    }

    // id 제외 모든 컬럼
    public List<TableField> getNonIdFields() {
        return allFields.stream()
                .filter(field -> !field.isId())
                .toList();
    }

    // 변경된 컬럼만 리턴
    public List<TableField> getChangeFields() {
        List<TableField> allFields = this.allFields;
        List<TableField> result = new ArrayList<>(allFields.size());

        for (int i = 0; i < allFields.size(); i++) {
            if (this.changedFields.get(i)) {
                result.add(allFields.get(i));
            }
        }

        return result;
    }

    // id를 포함 모든 컬럼
    public List<TableField> getAllFields() {
        return allFields;
    }

    public Class<E> getTableClass() {
        return tableClass;
    }

    public E getEntity() {
        return entity;
    }

    /**
     * 모든 필드를 주어진 필드로 교체한다.
     *
     * @param tableFields 교체할 필드
     */
    public void replaceAllFields(List<? extends TableField> tableFields) {
        Map<String, Object> fieldValueMap = tableFields.stream()
                .collect(Collectors.toMap(TableField::getFieldName, TableField::getFieldValue));

        for (TableField field : allFields) {
            Object fieldValue = fieldValueMap.get(field.getFieldName());
            field.setFieldValue(fieldValue);
        }
    }

    /**
     * TableField에 세팅된 값들을 엔티티 클래스의 값에 적용한다.
     */
    public void syncFieldValueToEntity() {
        // non-id field들의 fieldName과 fieldValue를 매핑
        Map<String, Object> classFieldNameMap = this.getNonIdFields().stream()
                .collect(Collectors.toMap(TableField::getClassFieldName, TableField::getFieldValue));

        // id field들의 fieldName과 fieldValue를 매핑
        classFieldNameMap.put(id.getClassFieldName(), id.getFieldValue());

        for (Field declaredField : tableClass.getDeclaredFields()) {
            var fieldValue = classFieldNameMap.get(declaredField.getName());
            setFieldValue(declaredField, fieldValue);
        }
    }

    private void setFieldValue(Field declaredField, Object fieldValue) {
        declaredField.setAccessible(true);
        try {
            if (fieldValue != null) {
                declaredField.set(entity, fieldValue);
            }
        } catch (IllegalAccessException e) {
            logger.error("Cannot access field: " + declaredField.getName(), e);
        }
    }

    private E createNewInstanceByDefaultConstructor(Class<E> entityClass) {
        try {
            Constructor<E> defaultConstructor = entityClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (
                NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e
        ) {
            logger.error(e.getMessage());
            throw new EntityHasNoDefaultConstructorException("entity must contain default constructor");
        }
    }

    private String extractTableName(Class<E> entityClass, JpaSettings settings) {
        return settings.getNamingStrategy().namingTable(entityClass);
    }

    /**
     * 엔티티로부터 ID 추출
     *
     * @return TablePrimaryField ID 필드
     * @throws InvalidIdMappingException ID 필드가 없거나 2개 이상인 경우
     */
    private TablePrimaryField extractId(JpaSettings settings) {
        EntityIdHolder<E> entityIdHolder = new EntityIdHolder<>(entity);
        Field idField = entityIdHolder.getIdField();
        return new TablePrimaryField(idField, entity, settings);
    }

    /**
     * 모든 영속성 필드 추출
     *
     * @param entityClass 엔티티 클래스
     * @return List<TableField> 모든 영속성 필드
     */
    private List<TableField> extractAllPersistenceFields(Class<E> entityClass, JpaSettings settings) {
        Field[] declaredFields = entityClass.getDeclaredFields();

        List<TableField> list = new ArrayList<>(declaredFields.length);
        for (Field declaredField : declaredFields) {
            boolean transientAnnotated = declaredField.isAnnotationPresent(Transient.class);
            boolean columnAnnotated = declaredField.isAnnotationPresent(Column.class);
            boolean idAnnotated = declaredField.isAnnotationPresent(Id.class);

            if (transientAnnotated && columnAnnotated) {
                throw new InvalidEntityException(String.format(
                        "class %s @Transient & @Column cannot be used in same field"
                        , entityClass.getName())
                );
            }

            if (!transientAnnotated) {
                list.add(
                        idAnnotated
                                ? new TablePrimaryField(declaredField, entity, settings)
                                : new TableField(declaredField, entity, settings)
                );
            }
        }
        return list;
    }
}
