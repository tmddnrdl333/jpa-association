package persistence.sql.ddl.query.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.fixture.PersonWithEntityIdFixture;
import persistence.sql.ddl.fixture.PersonWithGeneratedValueColumnFixture;
import persistence.sql.ddl.fixture.PersonWithTableTransientFixture;
import persistence.sql.dialect.H2Dialect;

public class CreateQueryBuilderTest {

    @Test
    @DisplayName("[성공] Person @Entity @Id 클래스에 대한 create query 검증")
    void createQueryWithEntityId() {
        String query = CreateQueryBuilder.builder(new H2Dialect())
                .create(PersonWithEntityIdFixture.class)
                .build();
        String expectedQuery = """
                create table PersonWithEntityIdFixture (id bigint auto_increment, name varchar(255), age integer, primary key (id))""";
        assertEquals(query, expectedQuery);
    }

    @Test
    @DisplayName("[성공] Person @GeneratedValue @Column 클래스에 대한 create query 검증")
    void createQueryWithGeneratedValueColumn() {
        String query = CreateQueryBuilder.builder(new H2Dialect())
                .create(PersonWithGeneratedValueColumnFixture.class)
                .build();
        String expectedQuery = """
                create table PersonWithGeneratedValueColumnFixture (id bigint auto_increment, nick_name varchar(255), old integer, email varchar(255) not null, primary key (id))""";
        assertEquals(query, expectedQuery);
    }

    @Test
    @DisplayName("[성공] Person @Table @Transient 클래스에 대한 create query 검증")
    void createQueryWithTableTransient() {
        String query = CreateQueryBuilder.builder(new H2Dialect())
                .create(PersonWithTableTransientFixture.class)
                .build();
        String expectedQuery = """
                create table users (id bigint auto_increment, nick_name varchar(255), old integer, email varchar(255) not null, primary key (id))""";

        assertEquals(query, expectedQuery.trim());
    }

}
