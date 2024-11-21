package sample.repository;

import java.sql.Connection;
import java.util.List;
import jdbc.JdbcTemplate;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.builder.SelectQueryBuilder;
import sample.domain.Person;

public class PersonRepository implements Repository<Person, Long> {

    private final JdbcTemplate jdbcTemplate;

    public PersonRepository(Connection connection) {
        this.jdbcTemplate = new JdbcTemplate(connection);
    }

    @Override
    public List<Person> findAll() {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = SelectQueryBuilder.builder()
                .select()
                .from(schemaMeta.tableName())
                .build();

        return jdbcTemplate.query(query, (resultSet -> new Person(
                resultSet.getLong("id"),
                resultSet.getString("nick_name"),
                resultSet.getInt("old"),
                resultSet.getString("email")
        )));
    }

    @Override
    public Person findById(Long id) {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = SelectQueryBuilder.builder()
                .select()
                .from(schemaMeta.tableName())
                .build();

        return jdbcTemplate.queryForObject(query, (resultSet -> new Person(
                resultSet.getLong("id"),
                resultSet.getString("nick_name"),
                resultSet.getInt("old"),
                resultSet.getString("email")
        )));
    }

}
