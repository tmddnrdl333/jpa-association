package orm.assosiation;

import jakarta.persistence.FetchType;
import orm.meta.EntityFieldMeta;
import orm.exception.EntityClassTypeNotInRelationException;
import orm.exception.NotYetImplementedException;
import orm.settings.JpaSettings;
import orm.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 엔티티의 연관관계 필드들에 대한 일급객체
 */
public class RelationFields<E> {

    private final List<RelationField> relationFieldList;

    public RelationFields(E entity, JpaSettings settings) {
        this.relationFieldList = extractRelationFieldsFromEntity(entity, settings);
    }

    public RelationFields(E entity) {
        this.relationFieldList = extractRelationFieldsFromEntity(entity, JpaSettings.ofDefault());
    }

    public RelationFields() {
        this.relationFieldList = Collections.emptyList();
    }

    public RelationFields(RelationFields<E> relationField) {
        this.relationFieldList = deepCopyRelationFields(relationField);
    }

    public List<RelationField> getRelationList() {
        return relationFieldList;
    }

    public int size() {
        return relationFieldList.size();
    }

    // 연관 관계 필드중 값이 있는 필드만 추출
    public List<RelationField> getValuedRelationList() {
        return relationFieldList.stream()
                .filter(RelationField::isValuedRelationField)
                .toList();
    }

    // 연관관계 필드중에 특정 클래스 타입인것들만 추출
    public RelationField getRelationFieldsOfType(Class<?> clazz) {
        return relationFieldList.stream()
                .filter(relationField -> relationField.tableEntityClass().equals(clazz))
                .findFirst()
                .orElseThrow(() -> new EntityClassTypeNotInRelationException("연관관계 목록에 해당 엔티티 타입이 없습니다" + clazz));
    }

    // EAGER 타입의 연관관계 필드만 추출
    public List<RelationField> getEagerRelationList() {
        return relationFieldList.stream()
                .filter(relationField -> relationField.getFetchType() == FetchType.EAGER)
                .toList();
    }

    public boolean hasRelation() {
        return CollectionUtils.isNotEmpty(relationFieldList);
    }

    private List<RelationField> deepCopyRelationFields(RelationFields<E> relationFields) {
        return relationFields.getRelationList().stream()
                .map(RelationField::new)
                .toList();
    }

    // 엔티티로부터 RelationField 추출
    private List<RelationField> extractRelationFieldsFromEntity(E entity, JpaSettings settings) {
        Class<?> entityClass = entity.getClass();
        Field[] declaredFields = entityClass.getDeclaredFields();

        // 연관관계 필드
        List<RelationField> associationList = new ArrayList<>(declaredFields.length);

        for (Field declaredField : declaredFields) {
            var entityProperty = new EntityFieldMeta(declaredField);

            // 연관 관계 분류 - 단일
            if (entityProperty.isManyToOneAssociated()) {
                throw new NotYetImplementedException("ManyToOne은 사용하지 않음");
            }

            // 연관 관계 분류 - 다중
            if (entityProperty.isOneToManyAssociated()) {
                associationList.add(RelationField.ofOneToManyRelation(entity, declaredField, settings));
            }
        }

        return associationList;
    }
}
