package persistence.entity;

import jdbc.EntityRowMapper;
import jdbc.JdbcTemplate;
import persistence.sql.definition.EntityTableMapper;
import persistence.sql.definition.TableDefinition;
import persistence.sql.dml.query.SelectQueryBuilder;

import java.util.Collection;

public class EntityLoader {
    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public <T> T loadEntity(Class<T> entityClass, EntityKey entityKey) {
        final SelectQueryBuilder queryBuilder = new SelectQueryBuilder(entityKey.entityClass());
        final TableDefinition tableDefinition = new TableDefinition(entityKey.entityClass());

        tableDefinition.getAssociations().forEach(association -> {
            if (association.isFetchEager()) {
                queryBuilder.join(association);
            }
        });

        final String query = queryBuilder.buildById(entityKey.id());

        final Object queried = jdbcTemplate.queryForObject(query,
                new EntityRowMapper<>(entityKey.entityClass())
        );

        return entityClass.cast(queried);
    }

    public <T> Collection<T> loadLazyCollection(Class<T> targetClass, EntityTableMapper ownerTableMapper) {
        final SelectQueryBuilder queryBuilder = new SelectQueryBuilder(targetClass);
        final String joinColumnName = ownerTableMapper.getJoinColumnName(targetClass);
        final Object value = ownerTableMapper.getValue(joinColumnName);

        final String query = queryBuilder
                .where(joinColumnName, value.toString())
                .build();

        return jdbcTemplate.query(query,
                new EntityRowMapper<>(targetClass)
        );
    }
}
