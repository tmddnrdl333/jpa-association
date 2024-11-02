package persistence.entity;

import database.H2ConnectionFactory;
import domain.OrderItem;
import domain.OrderLazy;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixture.EntityWithId;
import persistence.fixture.EntityWithOnlyId;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static util.QueryUtils.*;

class DefaultEntityManagerTest {
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate = new JdbcTemplate(H2ConnectionFactory.getConnection());

        createTable(EntityWithId.class);
        createTable(OrderLazy.class);
        createTable(OrderItem.class, OrderLazy.class);
    }

    @AfterEach
    void tearDown() {
        dropTable(EntityWithId.class);
        dropTable(OrderLazy.class);
        dropTable(OrderItem.class);
    }

    @Test
    @DisplayName("엔티티를 로드한다.")
    void find() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity, entityManager);

        // when
        final EntityWithId managedEntity = entityManager.find(entity.getClass(), entity.getId());

        // then
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isEqualTo(entity.getId()),
                () -> assertThat(managedEntity.getName()).isEqualTo(entity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(entity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(entity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNotNull()
        );
    }

    @Test
    @DisplayName("Lazy 연관관게를 가진 엔티티를 로드한다.")
    void find_withLazyAssociation() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final OrderLazy order = new OrderLazy("OrderNumber1");
        final OrderItem orderItem1 = new OrderItem("Product1", 10);
        final OrderItem orderItem2 = new OrderItem("Product2", 20);
        order.addOrderItem(orderItem1);
        order.addOrderItem(orderItem2);
        insertData(order, entityManager);
        insertData(orderItem1, order, entityManager);
        insertData(orderItem2, order, entityManager);
        entityManager.clear();

        // when
        final OrderLazy managedOrder = entityManager.find(order.getClass(), order.getId());

        // then
        assertAll(
                () -> assertThat(managedOrder).isNotNull(),
                () -> assertThat(managedOrder.getId()).isEqualTo(order.getId()),
                () -> assertThat(managedOrder.getOrderNumber()).isEqualTo(order.getOrderNumber()),
                () -> assertThat(managedOrder.getOrderItems()).hasSize(2),
                () -> assertThat(managedOrder.getOrderItems()).containsExactly(orderItem1, orderItem2)
        );
    }

    @Test
    @DisplayName("엔티티를 영속화한다.")
    void persist() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);

        // when
        entityManager.persist(entity);

        // then
        final EntityWithId managedEntity = entityManager.find(entity.getClass(), entity.getId());
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isNotNull(),
                () -> assertThat(managedEntity.getName()).isEqualTo(entity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(entity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(entity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNotNull()
        );
    }

    @Test
    @DisplayName("엔티티를 영속성 컨텍스트에 등록하고 flush() 한다.")
    void persistAndFlush() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithOnlyId entity = new EntityWithOnlyId(1L, "Jaden", 30, "test@email.com", 1);

        // when
        entityManager.persist(entity);
        entityManager.flush();

        // then
        final EntityWithOnlyId managedEntity = entityManager.find(entity.getClass(), entity.getId());
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isNotNull(),
                () -> assertThat(managedEntity.getName()).isEqualTo(entity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(entity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(entity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNotNull()
        );
    }

    @Test
    @DisplayName("엔티티를 영속성 컨텍스트에 등록하고 flush() 한다.")
    void persistAnRemoveAndFlush() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithOnlyId entity = new EntityWithOnlyId(1L, "Jaden", 30, "test@email.com", 1);

        // when
        entityManager.persist(entity);
        entityManager.remove(entity);
        entityManager.flush();

        // then
        assertThatThrownBy(() -> entityManager.find(entity.getClass(), entity.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected 1 result, got");
    }

    @Test
    @DisplayName("영속화 불가능한 상태에서 엔티티를 영속화하면 예외를 발생한다.")
    void persist_exception() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity, entityManager);

        // when & then
        assertThatThrownBy(() -> entityManager.persist(entity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(DefaultEntityManager.NOT_PERSISTABLE_STATUS_FAILED_MESSAGE);

    }

    @Test
    @DisplayName("엔티티를 영속성 상태에서 제거한다.")
    void removeAndFlush() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity, entityManager);

        // when
        entityManager.remove(entity);
        entityManager.flush();

        // then
        assertThatThrownBy(() -> entityManager.find(entity.getClass(), entity.getId()))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Expected 1 result, got");
    }

    @Test
    @DisplayName("제거 불가능한 상태에서 엔티티를 제거하면 예외를 발생한다.")
    void remove_exception() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        entityManager.persist(entity);
        entityManager.remove(entity);
        entityManager.flush();

        // when & then
        assertThatThrownBy(() -> entityManager.remove(entity))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(DefaultEntityManager.NOT_REMOVABLE_STATUS_FAILED_MESSAGE);

    }

    @Test
    @DisplayName("엔티티를 업데이트한다.")
    void updateAndFlush() {
        // given
        final EntityManager entityManager = DefaultEntityManager.of(jdbcTemplate);
        final EntityWithId entity = new EntityWithId("Jaden", 30, "test@email.com", 1);
        insertData(entity, entityManager);
        entity.setName("Yang");
        entity.setAge(35);
        entity.setEmail("test2@email.com");

        // when
        entityManager.flush();

        // then
        final EntityWithId managedEntity = entityManager.find(entity.getClass(), entity.getId());
        assertAll(
                () -> assertThat(managedEntity).isNotNull(),
                () -> assertThat(managedEntity.getId()).isEqualTo(entity.getId()),
                () -> assertThat(managedEntity.getName()).isEqualTo(entity.getName()),
                () -> assertThat(managedEntity.getAge()).isEqualTo(entity.getAge()),
                () -> assertThat(managedEntity.getEmail()).isEqualTo(entity.getEmail()),
                () -> assertThat(managedEntity.getIndex()).isNotNull()
        );
    }

    private void insertData(Object entity, EntityManager entityManager) {
        entityManager.persist(entity);
    }

    private void insertData(Object entity, Object parentEntity, EntityManager entityManager) {
        entityManager.persist(entity, parentEntity);
    }
}
