package persistence.sql.dml.query.builder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static persistence.sql.dml.query.WhereOperator.EQUAL;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.WhereCondition;
import sample.domain.Person;

class UpdateQueryBuilderTest {

    @Test
    @DisplayName("[성공] Person Entity 테이블의 특정 id에 대한 update query")
    void updateQuery() {
        Person person = new Person("person name", 20, "person@email.com");
        SchemaMeta schemaMeta = new SchemaMeta(person);
        UpdateQueryBuilder queryBuilder = UpdateQueryBuilder.builder()
                .update(schemaMeta.tableName())
                .set(schemaMeta.columnNamesWithoutPrimaryKey(), schemaMeta.columnValuesWithoutPrimaryKey())
                .where(List.of(new WhereCondition(schemaMeta.primaryKeyColumnName(), EQUAL, 1L)));
        String expectedQuery = """
                update users set nick_name = 'person name', old = 20, email = 'person@email.com' where id = 1""";
        assertEquals(queryBuilder.build(), expectedQuery);
    }

}
