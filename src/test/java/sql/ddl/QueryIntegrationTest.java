package sql.ddl;

import jdbc.JdbcTemplate;
import org.junit.jupiter.api.Test;
import persistence.sql.Dialect;
import persistence.sql.H2Dialect;
import persistence.sql.ddl.CreateQueryBuilder;
import persistence.sql.ddl.DropQueryBuilder;
import persistence.sql.ddl.Person;
import persistence.sql.ddl.QueryBuilder;
import persistence.sql.dml.DeleteQuery;
import persistence.sql.dml.InsertQuery;
import persistence.sql.dml.SelectQuery;
import persistence.sql.model.TableName;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcServerTest
public class QueryIntegrationTest {

    private static final String SELECT_TABLES_SQL = "SELECT table_name FROM information_schema.tables WHERE table_schema='PUBLIC'";
    private static final Dialect dialect = new H2Dialect();
    @Test
    void 테이블_생성() {
        Class<Person> clazz = Person.class;

        QueryBuilder createQueryBuilder = new CreateQueryBuilder(clazz, dialect);
        JdbcTemplate jdbcTemplate = JdbcServerExtension.getJdbcTemplate();
        jdbcTemplate.execute(createQueryBuilder.build());

        List<String> tableNames = jdbcTemplate.query(
                SELECT_TABLES_SQL,
                resultSet -> resultSet.getString("table_name")
        );

        TableName tableName = new TableName(clazz);
        assertThat(tableNames).contains(tableName.getValue().toUpperCase());
    }

    @Test
    void 테이블_제거() {
        Class<Person> clazz = Person.class;

        QueryBuilder createQueryBuilder = new CreateQueryBuilder(clazz, dialect);
        JdbcTemplate jdbcTemplate = JdbcServerExtension.getJdbcTemplate();
        jdbcTemplate.execute(createQueryBuilder.build());

        DropQueryBuilder dropQueryBuilder = new DropQueryBuilder(clazz);
        jdbcTemplate.execute(dropQueryBuilder.build());

        List<String> tableNames = jdbcTemplate.query(
                SELECT_TABLES_SQL,
                resultSet -> resultSet.getString("table_name")
        );

        assertThat(tableNames).isEmpty();
    }

    @Test
    void 데이터_삽입() {
        final String name = "이름";
        final Integer age = 11;
        final String email = "email@test.com";
        Person person = new Person(name, age, email, null);

        JdbcTemplate jdbcTemplate = JdbcServerExtension.getJdbcTemplate();

        QueryBuilder createQueryBuilder = new CreateQueryBuilder(Person.class, dialect);
        jdbcTemplate.execute(createQueryBuilder.build());

        InsertQuery insertQueryBuilder = InsertQuery.getInstance();
        jdbcTemplate.execute(insertQueryBuilder.makeQuery(person));
    }

    @Test
    void 데이터_삭제() {
        JdbcTemplate jdbcTemplate = JdbcServerExtension.getJdbcTemplate();

        QueryBuilder createQueryBuilder = new CreateQueryBuilder(Person.class, dialect);
        jdbcTemplate.execute(createQueryBuilder.build());

        final String name = "이름";
        final Integer age = 11;
        final String email = "email@test.com";
        Person person = new Person(name, age, email, null);
        InsertQuery insertQueryBuilder = InsertQuery.getInstance();
        jdbcTemplate.execute(insertQueryBuilder.makeQuery(person));

        SelectQuery selectQuery = SelectQuery.getInstance();
        Person findByIdPerson = jdbcTemplate.queryForObject(selectQuery.findById(Person.class, 1L), resultSet -> {
            long id = resultSet.getLong("id");
            int old = resultSet.getInt("old");
            String getEmail = resultSet.getString("email");
            String nickname = resultSet.getString("nick_name");
            return new Person(id, nickname, old, getEmail);
        });

        DeleteQuery deleteQueryBuilder = DeleteQuery.getInstance();
        jdbcTemplate.execute(deleteQueryBuilder.makeQuery(findByIdPerson));
    }

}