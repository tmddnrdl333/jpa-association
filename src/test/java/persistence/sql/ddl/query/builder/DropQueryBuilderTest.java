package persistence.sql.ddl.query.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.dialect.H2Dialect;
import sample.domain.Person;

class DropQueryBuilderTest {

    @Test
    @DisplayName("[성공] Person 테이블에 대한 drop query 검증")
    void dropQuery() {
        String query = DropQueryBuilder.builder(new H2Dialect())
                .drop(Person.class)
                .build();
        String dropQuery = "drop table if exists users";
        assertEquals(query, dropQuery);
    }

}
