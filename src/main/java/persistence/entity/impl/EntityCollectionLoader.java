package persistence.entity.impl;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import jdbc.JdbcTemplate;
import persistence.entity.EntityId;
import persistence.entity.Relation;
import persistence.sql.ddl.query.ColumnMeta;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.dml.query.WhereOperation;
import persistence.sql.dml.query.builder.SelectQueryBuilder;

public class EntityCollectionLoader {

    private final JdbcTemplate jdbcTemplate;

    public EntityCollectionLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> T loadCollection(Class<T> clazz, Object instance) {
        List<ColumnMeta> columnMetas = getColumnMetaHasRelation(clazz);
        for (ColumnMeta columnMeta : columnMetas) {
            Relation relation = columnMeta.relation();
            String query = SelectQueryBuilder.builder()
                    .select()
                    .from(relation.getJoinTableName())
                    .where(List.of(
                            new WhereCondition(
                                    relation.getJoinColumnName(),
                                    WhereOperation.EQUAL.value(),
                                    EntityId.getIdValue(instance)))
                    )
                    .build();

            List<?> children = jdbcTemplate.query(query, new EntityRowMapper<>(relation.getJoinColumnType()));
            mapChildrenField(instance, columnMeta, children);
        }

        return clazz.cast(instance);
    }

    private List<ColumnMeta> getColumnMetaHasRelation(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(field -> new ColumnMeta(field, clazz))
                .filter(columnMeta -> columnMeta.relation().hasRelation())
                .toList();
    }

    private void mapChildrenField(Object instance, ColumnMeta columnMeta, List<?> children) {
        try {
            Field field = columnMeta.field();
            field.setAccessible(true);
            field.set(instance, children);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
