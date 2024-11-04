package orm.dsl;

import config.PluggableH2test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.TableEntity;
import orm.dirty_check.DirtyCheckMarker;
import persistence.sql.ddl.Person;
import persistence.sql.ddl.mapper.PersonRowMapper;
import test_double.FakeQueryRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static orm.dsl.DSL.eq;
import static steps.Steps.Person_엔티티_생성;
import static steps.Steps.테이블_생성;

class QueryBuilderUpdateTest extends PluggableH2test {

    QueryBuilder queryBuilder;
    QueryRunner fakeQueryRunner;

    @BeforeEach
    void setUp() {
        queryBuilder = new QueryBuilder();
        fakeQueryRunner = new FakeQueryRunner();
    }

    @Test
    @DisplayName("UPDATE 절 생성 테스트 - 단순 id 기반")
    void UPDATE_문_테스트() {
        // given
        Person person = new Person(1L, 30, "설동민");

        // when
        String query = queryBuilder.update(person, fakeQueryRunner)
                .byId()
                .extractSql();

        // then
        assertThat(query).isEqualTo("UPDATE person SET name='설동민',age=30 WHERE id = 1");
    }

    @Test
    @DisplayName("UPDATE 절 더디 체킹 테스트")
    void UPDATE_문_더티체킹_테스트() {
        // given
        var oldPerson = new TableEntity<>(new Person(1L, 30, "설동민"));
        var person = new TableEntity<>(new Person(1L, 20, "설동민"));

        // when
        new DirtyCheckMarker<>(person, oldPerson).compareAndMarkChangedField(); // 더티체킹 진행
        String query = queryBuilder.update(person, fakeQueryRunner)
                .withBitsetAware() // 변경된 필드만 업데이트
                .byId()
                .extractSql();

        // then
        assertThat(query).isEqualTo("UPDATE person SET age=20 WHERE id = 1");
    }

    @Test
    @DisplayName("UPDATE 절 조건 포함 쿼리생성 테스트")
    void UPDATE_문_조건절_테스트() {
        // given
        Person person = new Person(1L, 20, "설동민");

        // when
        String query = queryBuilder.update(person, fakeQueryRunner)
                .where(
                        eq("id", 3L)
                                .and(eq("name", "이름 조건이요"))
                                .or(eq("age", 9999))
                )
                .extractSql();

        // then
        assertThat(query).isEqualTo("UPDATE person SET name='설동민',age=20 WHERE id = 3 AND name = '이름 조건이요' OR age = 9999");
    }

    @Test
    @DisplayName("DELETE 절 조건 실제 실행 테스트")
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

