package persistence.sql.dml;

import database.H2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import persistence.sql.dialect.DialectFactory;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DmlQueryBuilderTest {
    private static DmlQueryBuilder queryBuilder;
    private static final String testTableName = "users";
    private static final Map.Entry<String, Object> idColumnKeyValue = new AbstractMap.SimpleEntry<>("id", 1L);
    private static final Map.Entry<String, Object> nameKeyValue = new AbstractMap.SimpleEntry<>("nick_name", "홍길동");
    private static final Map.Entry<String, Object> ageKeyValue = new AbstractMap.SimpleEntry<>("old", 20);
    private static final Map.Entry<String, Object> emailKeyValue = new AbstractMap.SimpleEntry<>("email", "test@test.com");

    @BeforeEach
    void setup() {
        queryBuilder = new DmlQueryBuilder(DialectFactory.create(H2.class));
    }

    @Nested
    @DisplayName("Update 쿼리 생성 테스트")
    class UpdateQueryTests {
        @Test
        @DisplayName("엔티티 오브젝트의 PK를 기준으로 업데이트 쿼리를 생성한다.")
        void succeedToCreateQuery() {
            String expectedQuery = "UPDATE \"users\" " +
                    "SET \"id\" = 1, \"nick_name\" = '홍길동', \"old\" = 20, \"email\" = 'test@test.com' " +
                    "WHERE \"id\" = 1;";

            List<Map.Entry<String, Object>> updatingColumns = new ArrayList<>();
            updatingColumns.add(idColumnKeyValue);
            updatingColumns.add(nameKeyValue);
            updatingColumns.add(ageKeyValue);
            updatingColumns.add(emailKeyValue);

            String resultQuery = queryBuilder.buildUpdateQuery(
                    testTableName,
                    updatingColumns,
                    idColumnKeyValue
            );

            assertEquals(expectedQuery, resultQuery);
        }
    }

    @Nested
    @DisplayName("Insert 쿼리 생성 테스트")
    class InsertQueryTests {
        @Test
        @DisplayName("모든 필드의 값이 채워진 객체의 insert문을 생성한다.")
        void testCreateInsertQueryWithAllColumnsSet() {
            String expectedQuery = "INSERT INTO \"users\" " +
                    "(\"id\", \"nick_name\", \"old\", \"email\") " +
                    "VALUES (1, '홍길동', 20, 'test@test.com');";

            List<Map.Entry<String, Object>> updatingColumns = new ArrayList<>();
            updatingColumns.add(idColumnKeyValue);
            updatingColumns.add(nameKeyValue);
            updatingColumns.add(ageKeyValue);
            updatingColumns.add(emailKeyValue);

            String resultQuery = queryBuilder.buildInsertQuery(testTableName, updatingColumns);

            assertEquals(expectedQuery, resultQuery);
        }

        @Test
        @DisplayName("id가 없는 객체의 insert문을 생성한다.")
        void testCreateInsertQueryWithoutId() {
            String expectedQuery = "INSERT INTO \"users\" " +
                    "(\"nick_name\", \"old\", \"email\") " +
                    "VALUES ('홍길동', 20, 'test@test.com');";


            List<Map.Entry<String, Object>> updatingColumns = new ArrayList<>();
            updatingColumns.add(nameKeyValue);
            updatingColumns.add(ageKeyValue);
            updatingColumns.add(emailKeyValue);

            String resultQuery = queryBuilder.buildInsertQuery(testTableName, updatingColumns);

            assertEquals(expectedQuery, resultQuery);
        }

        @Test
        @DisplayName("null이 포함된 객체의 insert문을 생성한다.")
        void testCreateInsertQueryWithNull() {
            String expectedQuery = "INSERT INTO \"users\" " +
                    "(\"nick_name\", \"old\", \"email\") " +
                    "VALUES (NULL, NULL, 'test@test.com');";

            List<Map.Entry<String, Object>> updatingColumns = new ArrayList<>();
            updatingColumns.add(new AbstractMap.SimpleEntry<>("nick_name", null));
            updatingColumns.add(new AbstractMap.SimpleEntry<>("old", null));
            updatingColumns.add(emailKeyValue);

            String resultQuery = queryBuilder.buildInsertQuery(testTableName, updatingColumns);

            assertEquals(expectedQuery, resultQuery);
        }
    }

    @Nested
    @DisplayName("Select 쿼리 생성 테스트")
    class SelectQueryTests {
        @Test
        @DisplayName("pk로 테이블의 특정 필드를 조회한다.")
        void testCreateSelectSpecificColumnsQueryWithClauses() {
            String expectedQuery = "SELECT * FROM \"users\" " +
                    "WHERE (\"id\" = 1);";

            String resultQuery = queryBuilder.buildSelectByIdQuery(
                    testTableName,
                    idColumnKeyValue
            );

            assertEquals(expectedQuery, resultQuery);
        }
    }

    @Nested
    @DisplayName("Delete 쿼리 생성 테스트")
    class DeleteQueryTests {

        @Test
        @DisplayName("엔티티 객체가 주어지면 PK를 찾아 레코드를 삭제한다.")
        void succeedToDeleteByObject() {
            String expectedQuery = "DELETE FROM \"users\" WHERE \"id\" = 1;";

            String resultQuery = queryBuilder.buildDeleteQuery(
                    testTableName,
                    idColumnKeyValue
            );

            assertEquals(expectedQuery, resultQuery);
        }
    }
}
