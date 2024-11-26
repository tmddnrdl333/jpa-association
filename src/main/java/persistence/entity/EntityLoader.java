package persistence.entity;

import jdbc.EntityRowMapper;
import jdbc.JdbcTemplate;
import persistence.sql.component.ConditionBuilder;
import persistence.sql.component.TableInfo;
import persistence.sql.dml.select.SelectQuery;
import persistence.sql.dml.select.SelectQueryBuilder;

import java.util.Collections;

public class EntityLoader {
    private final JdbcTemplate jdbcTemplate;

    public EntityLoader(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Object find(Class<?> clazz, Long id) {
        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(new TableInfo(clazz))
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(EntityUtils.getIdColumn(clazz))
                                .values(Collections.singletonList(id.toString()))
                                .build()
                )
                .build();
        String query = selectQuery.toString();
        return jdbcTemplate.queryForObject(query, new EntityRowMapper<>(clazz));
    }
}
