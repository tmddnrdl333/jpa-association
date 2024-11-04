package orm;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Person;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

public class StatefulPersistenceContextTest extends PluggableH2test {

    @Test
    @DisplayName("persist 메서드를 사용하면 1차 캐시와 DB 스냅샷에 엔티티가 저장된다.")
    void persistence_테스트() {
        Map<EntityKey, Object> _1차캐시 = new HashMap<>();
        Map<EntityKey, Object> DB_스냅샷 = new HashMap<>();

        runInH2Db(queryRunner -> {
            // given
            PersistenceContext trackablePersistenceContext = new StatefulPersistenceContext(_1차캐시, DB_스냅샷);
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, trackablePersistenceContext);

            // when
            session.persist(new Person(1L, 30, "설동민"));

            // then
            assertThat(_1차캐시).hasSize(1);
            assertThat(DB_스냅샷).hasSize(1);
        });
    }

    @Test
    @DisplayName("merge 메서드를 사용하면 1차 캐시와 DB 스냅샷에 엔티티가 저장된다.")
    void merge_테스트() {
        Map<EntityKey, Object> _1차캐시 = new HashMap<>();
        Map<EntityKey, Object> DB_스냅샷 = new HashMap<>();

        runInH2Db(queryRunner -> {
            // given
            PersistenceContext trackablePersistenceContext = new StatefulPersistenceContext(_1차캐시, DB_스냅샷);
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, trackablePersistenceContext);

            Person origin = new Person(1L, 30, "설동민");
            session.persist(origin); // insert

            Person person = session.find(Person.class, 1L);
            person.setAge(20);

            // when
            session.merge(person); // update

            // then
            assertThat(_1차캐시).hasSize(1);
            assertThat(DB_스냅샷).hasSize(1);
        });
    }

    @Test
    @DisplayName("entity를 detach 하면 1차캐시와 DB스냅샷이 없다.")
    void detach_테스트() {
        Map<EntityKey, Object> _1차캐시 = new HashMap<>();
        Map<EntityKey, Object> DB_스냅샷 = new HashMap<>();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(_1차캐시, DB_스냅샷));
            session.persist(new Person(1L, 30, "설동민")); // insert

            Person person = session.find(Person.class, 1L);
            person.setAge(20);

            // when
            session.detach(person);

            // then
            assertThat(_1차캐시).isEmpty();
            assertThat(DB_스냅샷).isEmpty();
        });
    }
}
