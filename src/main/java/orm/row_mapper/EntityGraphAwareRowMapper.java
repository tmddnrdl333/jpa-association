package orm.row_mapper;

import jdbc.RowMapper;
import orm.TableEntity;
import orm.TableField;
import orm.assosiation.RelationField;
import orm.assosiation.RelationFields;
import orm.exception.RowMapperException;
import orm.meta.EntityFieldMeta;
import orm.meta.EntityFieldsMeta;
import orm.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 엔티티 연관관계를 인지한 row mapper
 *
 * @param <T>
 */
public class EntityGraphAwareRowMapper<T> implements RowMapper<T> {

    // 루트 엔티티
    private final TableEntity<T> rootTableEntity;

    // 루트 엔티티의 연관관계
    private final RelationFields<T> relationFields;

    // 다수의 ResultSet row에서 RootEntity의 Id값에 해당하는 row인이 찾기 위한 Map
    private final Map<Object, T> resultIdMap;

    // 엔티티 클래스별 필드 메타정보
    private final EntityFieldsMeta rootEntityFieldsMeta;

    public EntityGraphAwareRowMapper(TableEntity<T> rootTableEntity, RelationFields<T> relationFields) {
        this.rootTableEntity = rootTableEntity;
        this.relationFields = relationFields;
        this.resultIdMap = new HashMap<>();
        this.rootEntityFieldsMeta = new EntityFieldsMeta(rootTableEntity.getTableClass());
    }

    public T mapRow(ResultSet rs) throws RowMapperException {
        try {
            String idFieldName = rootTableEntity.getId().getFieldName();
            Object idValue = rs.getObject(idFieldName);

            T entity = mapRowIntoEntity(rs, idValue);

            // rs.next는 여기서만 호출된다.
            while (rs.next()) {
                Object newIdValue = rs.getObject(idFieldName);
                if (newIdValue.equals(idValue)) {
                    entity = mapRowIntoEntity(rs, idValue);
                    continue;
                }

                // 다음 row의 id값이 바뀌면 엔티티 매핑 종료
                return entity;
            }

            // 다음 row 가 없을 경우 매핑된 엔티티 반환
            return entity;
        } catch (Exception e) {
            throw new RowMapperException("Failed to map row to " + rootTableEntity.getTableClass().getName(), e);
        }
    }

    /**
     * resultSet의 하나의 row를 엔티티로 매핑한다.
     */
    private T mapRowIntoEntity(ResultSet rs, Object idValue) {

        // 루트 엔티티 부재시 생성
        T rootEntity = resultIdMap.computeIfAbsent(idValue, id ->
                new EntityMapper<>(rs, rootTableEntity).createEntityInstance()
        );

        // 관련된 연관 엔티티 매핑
        rootEntityFieldsMeta.getOneToManyRelationFields()
                .forEach(meta -> mapToManyRelation(rs, rootEntity, meta));

        return rootEntity;
    }

    /**
     * 연관관계 필드를 매핑한다.
     * OneToMany 연관관계는 루트 엔티티에 List를 생성하고, 하나의 row단위로만 연관관계를 세팅한다.
     *
     * @param rs              ResultSet
     * @param rootEntity      루트 엔티티
     * @param entityFieldMeta 엔티티 필드
     */
    private void mapToManyRelation(ResultSet rs, T rootEntity, EntityFieldMeta entityFieldMeta) {
        try {
            Field relationField = entityFieldMeta.field();
            List<Object> list = (List<Object>) relationField.get(rootEntity);
            if (list == null) {
                list = new ArrayList<>();
                ReflectionUtils.setFieldValue(relationField, rootEntity, list);
            }

            Class<?> relationClass = ReflectionUtils.extractGenericSignature(relationField);
            list.add(createRelationEntityInstance(rs, relationClass));
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // 연관관계 엔티티에 대한 값을 매핑
    public Object createRelationEntityInstance(ResultSet rs, Class<?> relationClass) {
        try {
            RelationField relationField = relationFields.getRelationFieldsOfType(relationClass);
            TableEntity<?> joinTableEntity = relationField.getJoinTableEntity();

            return new EntityMapper<>(rs, joinTableEntity)
                    .createEntityInstance();
        } catch (Exception e) {
            throw new RowMapperException("Failed to create new instance of " + relationClass.getName(), e);
        }
    }

    /**
     * ResultSet의 row를 엔티티로 매핑하는 Mapper
     * @param <T>
     */
    public static class EntityMapper<T> {

        private final ResultSet rs;
        private final TableEntity<T> tableEntity;

        public EntityMapper(ResultSet rs, TableEntity<T> tableEntity) {
            this.rs = rs;
            this.tableEntity = tableEntity;
        }

        // 루트 엔티티에 대한 값을 매핑
        public T createEntityInstance() {
            Class<T> tableClass = tableEntity.getTableClass();
            try {
                T entity = tableClass.getDeclaredConstructor().newInstance();
                mapColumnToEntity(rs, entity, tableEntity);
                return entity;
            } catch (Exception e) {
                throw new RowMapperException("Failed to create new instance of " + tableClass.getName(), e);
            }
        }

        protected void mapColumnToEntity(ResultSet rs, Object entity, TableEntity<T> tableEntity) throws SQLException {
            final EntityFieldsMeta entityFieldsMeta = new EntityFieldsMeta(entity.getClass());
            final String tableAlias = tableEntity.getAliasName();

            // 연관 엔티티 << 컬럼명, 테이블 필드 >> 매핑
            Map<String, TableField> classFieldMap = tableEntity.getAllFields().stream()
                    .collect(Collectors.toMap(TableField::getClassFieldName, Function.identity()));

            var entityFieldProperties = entityFieldsMeta.getNonRelationFields();
            for (EntityFieldMeta entityFieldMeta : entityFieldProperties) {
                boolean isTableFieldPartOfEntityField = classFieldMap.containsKey(entityFieldMeta.getFieldName());
                if (isTableFieldPartOfEntityField) {
                    ReflectionUtils.setFieldValue(entityFieldMeta.getField(), entity, rs.getObject("%s_%s".formatted(tableAlias, entityFieldMeta.getClassFieldName())));
                }
            }
        }
    }
}

