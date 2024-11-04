package orm.row_mapper;

import config.PluggableH2test;
import jdbc.RowMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.dsl.QueryBuilder;
import persistence.sql.ddl.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

class RowMapperTest extends PluggableH2test {

    @Test
    @DisplayName("""
            RowMapper 인터페이스를 구현한 DefaultRowMapper 클래스를 테스트한다
            내부적으로 TableEntity를 사용하여 컬럼값을 매칭해서 가져온다.
            """)
    void rowMapper_테스트() {
        runInH2Db(queryRunner -> {
            // given
            final Long personId = 1L;
            QueryBuilder queryBuilder = new QueryBuilder();

            테이블_생성(queryRunner, Person.class);
            Person newPerson = new Person(personId, 30, "설동민");
            queryBuilder.insertIntoValues(newPerson, queryRunner).execute();

            // when
            RowMapper<Person> rowMapper = new DefaultRowMapper<>(newPerson);
            Person person = queryBuilder.selectFrom(Person.class, queryRunner)
                    .fetchOne(rowMapper);

            // then
            assertThat(person)
                    .satisfies(p -> {
                        assertThat(p.getId()).isEqualTo(personId);
                        assertThat(p.getAge()).isEqualTo(30);
                        assertThat(p.getName()).isEqualTo("설동민");
                    });
        });
    }
}
