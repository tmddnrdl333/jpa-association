package orm;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.dsl.QueryBuilder;
import persistence.sql.ddl.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

class DefaultEntityLoaderTest extends PluggableH2test {

    @Test
    @DisplayName("""
            엔티티 로더는 하이버네이트 1차캐시와 관련 없기 때문에
            저장한 객체의 Equality가 같아도 Identity가 같지 않다.
            """)
    void 엔티티_로더_테스트() {
        runInH2Db((queryRunner) -> {
            // given
            테이블_생성(queryRunner, Person.class);
            QueryBuilder queryBuilder = new QueryBuilder();
            EntityPersister entityManager = new DefaultEntityPersister(queryBuilder, queryRunner);
            Person newPerson = new Person(1L, 30, "설동민");
            entityManager.persist(newPerson);

            // when
            EntityLoader entityLoader = new DefaultEntityLoader(queryBuilder, queryRunner);
            Person person = entityLoader.find(Person.class, 1L);

            // then
            assertThat(person)
                    .isNotSameAs(newPerson)
                    .satisfies(p -> {
                        assertThat(p.getId()).isEqualTo(newPerson.getId());
                        assertThat(p.getAge()).isEqualTo(newPerson.getAge());
                        assertThat(p.getName()).isEqualTo(newPerson.getName());
                    });
        });
    }
}
