package util;

import database.H2ConnectionFactory;
import jdbc.JdbcTemplate;
import persistence.dialect.H2Dialect;
import persistence.sql.ddl.CreateQuery;
import persistence.sql.ddl.DropQuery;

public class QueryUtils {
    private QueryUtils() {
        throw new AssertionError();
    }

    public static void createTable(Class<?> entityType) {
        final CreateQuery createQuery = new CreateQuery(entityType, new H2Dialect());
        JdbcTemplate jdbcTemplate  = new JdbcTemplate(H2ConnectionFactory.getConnection());
        jdbcTemplate.execute(createQuery.create());
    }

    public static void createTable(Class<?> entityType, Class<?> parentEntityType) {
        final CreateQuery createQuery = new CreateQuery(entityType, new H2Dialect());
        JdbcTemplate jdbcTemplate  = new JdbcTemplate(H2ConnectionFactory.getConnection());
        jdbcTemplate.execute(createQuery.create(parentEntityType));
    }

    public static void dropTable(Class<?> entityType) {
        final DropQuery dropQuery = new DropQuery(entityType);
        JdbcTemplate jdbcTemplate  = new JdbcTemplate(H2ConnectionFactory.getConnection());
        jdbcTemplate.execute(dropQuery.drop());
    }
}
