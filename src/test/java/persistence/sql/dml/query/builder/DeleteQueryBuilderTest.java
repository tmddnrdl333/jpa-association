package persistence.sql.dml.query.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static persistence.sql.dml.query.WhereOperator.EQUAL;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.WhereCondition;
import sample.domain.Person;

public class DeleteQueryBuilderTest {

    @Test
    @DisplayName("[성공] Person Entity 테이블의 모든 컬럼에 대한 delete query")
    void deleteQuery() {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = DeleteQueryBuilder.builder()
                .delete(schemaMeta.tableName())
                .build();
        String expectedQuery = """
                delete from users""";
        assertEquals(query, expectedQuery);
    }

    @Test
    @DisplayName("[성공] Person Entity 테이블의 특정 id 컬럼에 대한 delete query")
    void deleteQueryWhereId() {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = DeleteQueryBuilder.builder()
                .delete(schemaMeta.tableName())
                .where(List.of(new WhereCondition("id", EQUAL, 1L)))
                .build();
        String expectedQuery = """
                delete from users where id = 1""";
        assertEquals(query, expectedQuery);
    }

}
