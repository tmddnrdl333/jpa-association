package persistence.entity;

import jdbc.JdbcTemplate;
import jdbc.RowMapperFactory;
import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.definition.TableDefinition;
import persistence.sql.dml.query.SelectQueryBuilder;

public class EntityLoader {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapperFactory rowMapperFactory;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.rowMapperFactory = RowMapperFactory.getInstance();
    }

    public <T> T loadEntity(Class<T> entityClass, EntityKey entityKey) {
        final SelectQueryBuilder queryBuilder = new SelectQueryBuilder(entityKey.entityClass());
        final TableDefinition tableDefinition = new TableDefinition(entityKey.entityClass());

        tableDefinition.getAssociations().stream()
                .filter(TableAssociationDefinition::isEager)
                .forEach(queryBuilder::join);

        final String query = queryBuilder.buildById(entityKey.id());

        final Object queried = jdbcTemplate.queryForObject(query,
                rowMapperFactory.createRowMapper(entityClass, jdbcTemplate)
        );

        return entityClass.cast(queried);
    }
}
