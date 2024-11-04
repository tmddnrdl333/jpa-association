package orm.life_cycle;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.EntityManager;
import orm.SessionImpl;
import orm.StatefulPersistenceContext;
import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import persistence.sql.ddl.Person;
import test_entity.PersonWithAI;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.테이블_생성;

public class EntityEntryContextTest extends PluggableH2test {

    @Test
    @DisplayName("[클라이언트에서 생성되는 ID] 신규 엔티티를 저장하면 SAVING -> MANAGED 상태로 저장된다.")
    void entityEntry_persist_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);
            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            Person origin = new Person(1L, 30, "설동민");

            // when
            session.persist(origin); // insert
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(origin);

            // then
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.SAVING, Status.MANAGED);
        });
    }

    @Test
    @DisplayName("[DB에서 생성되는 ID] 신규 엔티티를 저장하면 SAVING 없이 바로 MANAGED 상태로 저장된다.")
    void entityEntry_persist_ai_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, PersonWithAI.class);
            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            PersonWithAI origin = new PersonWithAI(30L, "설동민");

            // when
            session.persist(origin); // insert

            // then
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(origin);
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.MANAGED);
        });
    }

    @Test
    @DisplayName("엔티티를 조회하면 LOADING 상태를 거쳐서 MANAGED 상태로 저장된다.")
    void entityEntry_find_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);
            엔티티매니저_없이_insert(new Person(1L, 30, "설동민"), queryRunner);

            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));

            // when
            Person person = session.find(Person.class, 1L);
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(person);

            // then
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.LOADING, Status.MANAGED);
        });
    }

    @Test
    @DisplayName("엔티티에 persist 후 find를 하면 SAVING -> MANAGED 상태가 된다.")
    void entityEntry_insert_후_find_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);

            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));

            // when
            session.persist(new Person(1L, 30, "설동민"));
            session.find(Person.class, 1L);

            // then
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(Person.class, 1L);
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.SAVING, Status.MANAGED);
        });
    }

    @Test
    @DisplayName("엔티티에 persist 후 delete 하면 SAVING -> MANAGED -> DELETE -> GONE 상태가 된다.")
    void entityEntry_insert_후_delete_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);

            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            Person person = new Person(1L, 30, "설동민");

            // when
            session.persist(person);
            session.remove(person);

            // then
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(Person.class, person.getId());
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.SAVING, Status.MANAGED, Status.DELETED, Status.GONE);
        });
    }

    @Test
    @DisplayName("엔티티 조회 후 detach 이후 재조회하면 LOADING -> MANAGED -> GONE -> LOADING -> MANAGED 상태가 된다.")
    void entityEntry_find_detach_find_테스트() {
        TrackableEntityEntryContext entityEntryContext = new TrackableEntityEntryContext();

        runInH2Db(queryRunner -> {
            // given
            테이블_생성(queryRunner, Person.class);

            EntityManager session = new SessionImpl(queryRunner, new StatefulPersistenceContext(entityEntryContext));
            Person person = new Person(1L, 30, "설동민");
            엔티티매니저_없이_insert(person, queryRunner);

            // when
            session.find(Person.class, person.getId());
            session.detach(person);
            session.find(Person.class, person.getId());

            // then
            List<EntityEntry> entryChangeLog = entityEntryContext.getEntryChangeLog(Person.class, person.getId());
            assertThat(entryChangeLog).asList()
                    .extracting("status")
                    .containsExactly(Status.LOADING, Status.MANAGED, Status.GONE, Status.LOADING, Status.MANAGED);
        });
    }

    public void 엔티티매니저_없이_insert(Person origin, QueryRunner queryRunner) {
        new QueryBuilder()
                .insertIntoValues(origin, queryRunner)
                .execute();
    }
}
