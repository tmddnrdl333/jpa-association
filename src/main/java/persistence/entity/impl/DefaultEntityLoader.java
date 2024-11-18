package persistence.entity.impl;

import java.util.Arrays;
import java.util.List;
import jdbc.JdbcTemplate;
import persistence.entity.EntityLoader;
import persistence.entity.Relation;
import persistence.sql.ddl.query.ColumnMeta;
import persistence.sql.dml.query.SelectQuery;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.dml.query.builder.SelectQueryBuilder;

public class DefaultEntityLoader implements EntityLoader {

    private final JdbcTemplate jdbcTemplate;
    private final EntityCollectionLoader collectionLoader;

    public DefaultEntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.collectionLoader = new EntityCollectionLoader(jdbcTemplate);
    }

    @Override
    public <T> T load(Class<T> clazz, Object id) {
        SelectQuery query = new SelectQuery(clazz);
        String queryString = SelectQueryBuilder.builder()
                .select(query.columnNames())
                .from(query.tableName())
                .where(List.of(new WhereCondition("id", "=", id)))
                .build();

        T instance = jdbcTemplate.queryForObject(queryString, new EntityRowMapper<>(clazz));
        if (hasNotRelation(clazz)) {
            return instance;
        }

        return collectionLoader.loadCollection(clazz, instance);
    }

    private boolean hasRelation(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .map(field -> new ColumnMeta(field, clazz))
                .map(ColumnMeta::relation)
                .anyMatch(Relation::hasRelation);
    }

    private boolean hasNotRelation(Class<?> clazz) {
        return !hasRelation(clazz);
    }

}
