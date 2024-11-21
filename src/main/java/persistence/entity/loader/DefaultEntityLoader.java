package persistence.entity.loader;

import java.util.List;
import jdbc.JdbcTemplate;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.dml.query.WhereOperator;
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
        SchemaMeta schemaMeta = new SchemaMeta(clazz);
        String query = SelectQueryBuilder.builder()
                .select()
                .from(schemaMeta.tableName())
                .where(List.of(new WhereCondition(schemaMeta.primaryKeyColumnName(), WhereOperator.EQUAL, id)))
                .build();

        T instance = jdbcTemplate.queryForObject(query, new EntityRowMapper<>(clazz));
        if (schemaMeta.hasNotRelation()) {
            return instance;
        }

        return collectionLoader.loadCollection(clazz, instance);
    }



}
