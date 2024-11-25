package persistence.entity;

import jdbc.EntityRowMapper;
import jdbc.JdbcTemplate;
import persistence.sql.dml.select.SelectQueryBuilder;

public class EntityLoader {
    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Object find(Class<?> clazz, Long id) {
        String findByIdQuery = SelectQueryBuilder.generateQuery(clazz, id);
        return jdbcTemplate.queryForObject(findByIdQuery, new EntityRowMapper<>(clazz));
    }
}
