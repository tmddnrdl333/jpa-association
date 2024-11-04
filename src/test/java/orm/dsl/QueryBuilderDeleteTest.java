package orm.dsl;

import config.PluggableH2test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Person;
import persistence.sql.ddl.mapper.PersonRowMapper;
import test_double.FakeQueryRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static orm.dsl.DSL.eq;
import static steps.Steps.Person_엔티티_생성;
import static steps.Steps.테이블_생성;

class QueryBuilderDeleteTest extends PluggableH2test {

    QueryBuilder queryBuilder;
    QueryRunner fakeQueryRunner;

    @BeforeEach
    void setUp() {
        queryBuilder = new QueryBuilder();
        fakeQueryRunner = new FakeQueryRunner();
    }

    @Test
    @DisplayName("DELETE 절 생성 테스트")
    void DML_DELETE_문_테스트() {
        // when
        String query = queryBuilder.deleteFrom(Person.class, fakeQueryRunner)
                .extractSql();

        // then
        assertThat(query).isEqualTo("DELETE FROM person");
    }

    @Test
    @DisplayName("DELETE 절 조건 포함 쿼리생성 테스트")
    void DML_DELETE_문_조건절_테스트() {
        // given
        // when
        String query = queryBuilder.deleteFrom(Person.class, fakeQueryRunner)
                .where(
                    eq("id", 1L)
                        .and(eq("name", "설동민"))
                        .or(eq("age", 30))
                )
                .extractSql();

        // then
        assertThat(query).isEqualTo("DELETE FROM person WHERE id = 1 AND name = '설동민' OR age = 30");
    }

    @Test
    @DisplayName("DELETE 절 조건 실행 테스트")
    void DML_DELETE_문_실행_테스트() {
        runInH2Db((queryRunner) -> {
            // given
            QueryBuilder queryBuilder = new QueryBuilder();

            테이블_생성(queryRunner, Person.class);
            Person_엔티티_생성(queryRunner, new Person(1L, 30, "설동민"));
            Person_엔티티_생성(queryRunner, new Person(2L, 30, "설동민2"));

            // when
            queryBuilder.deleteFrom(Person.class, queryRunner)
                    .where(eq("id", 1).or(eq("id", 2L)))
                    .execute();

            List<Person> people = queryBuilder.selectFrom(Person.class, queryRunner)
                    .findAll()
                    .fetch(new PersonRowMapper());

            // then
            assertThat(people).asList().hasSize(0);
        });
    }
}

