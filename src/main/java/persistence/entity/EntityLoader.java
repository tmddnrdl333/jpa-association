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

    public Object find(Class<?> entityClass, Long id) {
        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(TableInfo.from(entityClass))
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(EntityUtils.getIdColumn(entityClass))
                                .values(Collections.singletonList(id.toString()))
                                .build()
                )
                .build();
        String query = selectQuery.toString();
        return jdbcTemplate.queryForObject(query, new EntityRowMapper<>(entityClass));
    }
}
