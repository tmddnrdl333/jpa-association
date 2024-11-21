package persistence.sql.dml.query.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static persistence.sql.dml.query.WhereOperator.EQUAL;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.WhereCondition;
import sample.domain.Person;

public class SelectQueryBuilderTest {

    @Test
    @DisplayName("[성공] Person Entity 테이블의 모든 컬럼에 대한 select query")
    void selectQuery() {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = SelectQueryBuilder.builder()
                .select(schemaMeta.columnNames(), schemaMeta.tableName())
                .from(schemaMeta.tableName())
                .build();
        String expectedQuery = """
                select users.id, users.nick_name, users.old, users.email from users""";
        assertEquals(query, expectedQuery);
    }

    @Test
    @DisplayName("[성공] Person Entity 테이블의 id 컬럼에 대한 select query")
    void selectQueryById() {
        SchemaMeta schemaMeta = new SchemaMeta(Person.class);
        String query = SelectQueryBuilder.builder()
                .select(schemaMeta.columnNames(), schemaMeta.tableName())
                .from(schemaMeta.tableName())
                .where(List.of(new WhereCondition(schemaMeta.primaryKeyColumnName(), EQUAL, 1L)))
                .build();

        String expectedQuery = """
                select users.id, users.nick_name, users.old, users.email from users where id = 1""";
        assertEquals(query, expectedQuery);
    }

}
