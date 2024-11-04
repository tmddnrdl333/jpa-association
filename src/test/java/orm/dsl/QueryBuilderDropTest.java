package orm.dsl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import test_double.FakeQueryRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static util.SQLUtil.SQL_노멀라이즈;

class QueryBuilderDropTest {

    QueryBuilder queryBuilder;
    QueryRunner fakeQueryRunner;

    @BeforeEach
    void setUp() {
        queryBuilder = new QueryBuilder();
        fakeQueryRunner = new FakeQueryRunner();
    }

    @Test
    @DisplayName("DROP 절 생성 테스트")
    void DDL_DROP_절_테스트_1() {

        // given
        Class<DummyEntity> entityClass = DummyEntity.class;
        String expectedQuery = SQL_노멀라이즈(
                """
                        DROP TABLE test_table;
                        """
        );

        // when
        String query = SQL_노멀라이즈(
                queryBuilder.dropTable(entityClass, fakeQueryRunner)
                        .extractSql()
        );

        // then
        assertThat(query).isEqualTo(expectedQuery);
    }

    @Test
    @DisplayName("DROP 절 생성 테스트 + IF NOT EXISTS 문 추가")
    void DDL_DROP_절_테스트_2() {

        // given
        Class<DummyEntity> entityClass = DummyEntity.class;
        String expectedQuery = SQL_노멀라이즈(
                """
                        DROP TABLE IF NOT EXISTS test_table;
                        """
        );

        // when
        String query = SQL_노멀라이즈(
                queryBuilder.dropTable(entityClass, fakeQueryRunner)
                        .ifNotExist()
                        .extractSql()
        );

        // then
        assertThat(query).isEqualTo(expectedQuery);
    }
}
