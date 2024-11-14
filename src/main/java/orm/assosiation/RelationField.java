package orm.assosiation;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import orm.TableEntity;
import orm.settings.JpaSettings;
import orm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static java.util.function.Predicate.not;
import static orm.util.ReflectionUtils.extractGenericSignature;

public class RelationField {

    // 연관관계 테이블정보
    private final TableEntity<?> tableEntity;

    // 연관관계 테이블의 조인컬럼명
    private final String joinColumnName;

    // 연관관계 필드의 값
    private final Object value;

    private final FetchType fetchType;

    private final boolean isValueTypeCollection;

    private RelationField(Object value, boolean isValueTypeCollection, FetchType fetchType, TableEntity<?> tableEntity, String joinColumnName) {
        this.value = value;
        this.isValueTypeCollection = isValueTypeCollection;
        this.fetchType = fetchType;
        this.joinColumnName = joinColumnName;
        this.tableEntity = tableEntity;
    }

    public RelationField(RelationField relationField) {
        this.value = relationField.value;
        this.isValueTypeCollection = relationField.isValueTypeCollection;
        this.fetchType = relationField.fetchType;
        this.joinColumnName = relationField.joinColumnName;
        this.tableEntity = deepCopyTableEntity(relationField.tableEntity);
    }

    public static RelationField ofOneToManyRelation(Object entity, Field field, JpaSettings settings) {
        final var tableEntity = new TableEntity<>(extractGenericSignature(field), settings).addAliasIfNotAssigned();
        final var idField = tableEntity.getId();

        final OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        final String joinColumnName = Optional.ofNullable(field.getAnnotation(JoinColumn.class).name())
                .filter(not(String::isBlank))
                .orElse(idField.getFieldName());

        Object value = ReflectionUtils.getFieldValueFromObject(entity, field);

        return new RelationField(value, true, oneToMany.fetch(), tableEntity, joinColumnName);
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public String getJoinColumnName() {
        return joinColumnName;
    }

    public TableEntity<?> getJoinTableEntity() {
        return tableEntity;
    }

    public Class<?> tableEntityClass() {
        return tableEntity.getTableClass();
    }

    public String getAliasName() {
        return tableEntity.getAliasName();
    }

    public String getTableName() {
        return tableEntity.getTableName();
    }

    public boolean isValuedRelationField() {
        return value != null;
    }

    public List<Object> getValueAsList() {
        if (isValueTypeCollection) {
            return (List<Object>) value;
        }

        throw new UnsupportedOperationException("해당 필드는 컬렉션 타입이 아닙니다.");
    }

    public boolean isValueTypeCollection() {
        return isValueTypeCollection;
    }

    private TableEntity<?> deepCopyTableEntity(TableEntity<?> tableEntity) {
        var newTableEntity = new TableEntity<>(tableEntity.getEntity());
        if (tableEntity.hasAlias()) {
            newTableEntity.addAliasIfNotAssigned(tableEntity.getAlias());
        }
        return newTableEntity;
    }
}
