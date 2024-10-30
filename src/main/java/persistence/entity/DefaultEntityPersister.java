package persistence.entity;

import jdbc.DefaultIdMapper;
import jdbc.JdbcTemplate;
import persistence.meta.EntityColumn;
import persistence.sql.dml.DeleteQuery;
import persistence.sql.dml.InsertQuery;
import persistence.sql.dml.UpdateQuery;

import java.util.List;

public class DefaultEntityPersister implements EntityPersister {
    
    private final JdbcTemplate jdbcTemplate;
    private final InsertQuery insertQuery;
    private final UpdateQuery updateQuery;
    private final DeleteQuery deleteQuery;

    public DefaultEntityPersister(JdbcTemplate jdbcTemplate, InsertQuery insertQuery,
                                  UpdateQuery updateQuery, DeleteQuery deleteQuery) {
        this.jdbcTemplate = jdbcTemplate;
        this.insertQuery = insertQuery;
        this.updateQuery = updateQuery;
        this.deleteQuery = deleteQuery;
    }

    @Override
    public void insert(Object entity) {
        final String sql = insertQuery.insert(entity);
        jdbcTemplate.executeAndReturnGeneratedKeys(sql, new DefaultIdMapper(entity));
    }

    @Override
    public void insert(Object entity, Object parentEntity) {
        final String sql = insertQuery.insert(entity, parentEntity);
        jdbcTemplate.executeAndReturnGeneratedKeys(sql, new DefaultIdMapper(entity));
    }

    @Override
    public void update(Object entity, List<EntityColumn> entityColumns) {
        jdbcTemplate.execute(updateQuery.update(entity, entityColumns));
    }

    @Override
    public void delete(Object entity) {
        jdbcTemplate.execute(deleteQuery.delete(entity));
    }
}
