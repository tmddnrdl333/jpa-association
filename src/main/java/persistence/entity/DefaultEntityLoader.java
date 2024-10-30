package persistence.entity;

import jdbc.DefaultRowMapper;
import jdbc.JdbcTemplate;
import persistence.sql.dml.SelectQuery;

public class DefaultEntityLoader implements EntityLoader {
    private final JdbcTemplate jdbcTemplate;
    private final SelectQuery selectQuery;

    public DefaultEntityLoader(JdbcTemplate jdbcTemplate, SelectQuery selectQuery) {
        this.jdbcTemplate = jdbcTemplate;
        this.selectQuery = selectQuery;
    }

    @Override
    public <T> T load(Class<T> entityType, Object id) {
        final String sql = selectQuery.findById(entityType, id);
        return jdbcTemplate.queryForObject(sql, new DefaultRowMapper<>(entityType));
    }
}
