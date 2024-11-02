package persistence.entity;

import database.DatabaseServer;
import database.H2;
import domain.Order;
import domain.OrderItem;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixtures.TestEagerOrder;
import persistence.fixtures.TestEagerOrderItem;
import persistence.fixtures.TestLazyOrder;
import persistence.fixtures.TestLazyOrderItem;
import persistence.proxy.PersistentList;
import persistence.sql.H2Dialect;
import persistence.sql.SqlType;
import persistence.sql.ddl.query.CreateTableQueryBuilder;
import persistence.sql.ddl.query.DropQueryBuilder;
import persistence.sql.definition.ColumnDefinitionAware;

import java.lang.reflect.Proxy;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EntityManagerTest {

    @Entity
    public static class EntityManagerTestEntityWithIdentityId {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String name;

        private Integer age;

        public EntityManagerTestEntityWithIdentityId() {
        }

        public EntityManagerTestEntityWithIdentityId(String name, Integer age) {
            this.name = name;
            this.age = age;
        }

        public EntityManagerTestEntityWithIdentityId(Long id, String name, Integer age) {
            this.id = id;
            this.name = name;
            this.age = age;
        }
    }

    private static DatabaseServer server;
    private static JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws SQLException {
        server = new H2();
        server.start();

        String query = new CreateTableQueryBuilder(new H2Dialect(), EntityManagerTestEntityWithIdentityId.class, List.of()).build();
        String query2 = new CreateTableQueryBuilder(new H2Dialect(), Order.class, List.of()).build();
        String query3 = new CreateTableQueryBuilder(new H2Dialect(), OrderItem.class, Collections.singletonList(new ColumnDefinitionAware() {
            @Override
            public String getDatabaseColumnName() {
                return "order_id";
            }

            @Override
            public String getEntityFieldName() {
                return "id";
            }

            @Override
            public boolean isNullable() {
                return true;
            }

            @Override
            public int getLength() {
                return 0;
            }

            @Override
            public SqlType getSqlType() {
                return SqlType.BIGINT;
            }

            @Override
            public boolean isPrimaryKey() {
                return false;
            }
        })).build();

        jdbcTemplate = new JdbcTemplate(server.getConnection());
        jdbcTemplate.execute(query);
        jdbcTemplate.execute(query2);
        jdbcTemplate.execute(query3);

        jdbcTemplate.execute(TestLazyOrder.createTableQuery());
        jdbcTemplate.execute(TestEagerOrder.createTableQuery());
        jdbcTemplate.execute(TestLazyOrderItem.createTableQuery());
        jdbcTemplate.execute(TestEagerOrderItem.createTableQuery());
    }

    @AfterEach
    void tearDown() throws SQLException {
        String query = new DropQueryBuilder(EntityManagerTestEntityWithIdentityId.class).build();
        String query2 = new DropQueryBuilder(Order.class).build();
        String query3 = new DropQueryBuilder(OrderItem.class).build();

        jdbcTemplate = new JdbcTemplate(server.getConnection());
        jdbcTemplate.execute(query);
        jdbcTemplate.execute(query2);
        jdbcTemplate.execute(query3);

        jdbcTemplate.execute(new DropQueryBuilder(TestLazyOrder.class).build());
        jdbcTemplate.execute(new DropQueryBuilder(TestEagerOrder.class).build());
        jdbcTemplate.execute(new DropQueryBuilder(TestLazyOrderItem.class).build());
        jdbcTemplate.execute(new DropQueryBuilder(TestEagerOrderItem.class).build());
        server.stop();
    }

    @Test
    @DisplayName("Identity 전략을 사용하는 엔티티를 저장한다.")
    void testPersistWithIdentityId() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId(null, "john_doe", 30);
        entityManager.persist(entity);

        EntityManagerTestEntityWithIdentityId persistedEntity = entityManager.find(EntityManagerTestEntityWithIdentityId.class, 1L);
        assertAll(
                () -> assertThat(persistedEntity.id).isEqualTo(1L),
                () -> assertThat(persistedEntity.name).isEqualTo("john_doe"),
                () -> assertThat(persistedEntity.age).isEqualTo(30)
        );
    }

    @Test
    @DisplayName("Identity 전략을 사용하지만, id값이 있는 경우 에러가 발생한다.")
    void testPersistWithIdentityIdButId() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId(1L, "john_doe", 30);

        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> entityManager.persist(entity));
        assertThat(e.getMessage()).isEqualTo("No Entity Entry with id: 1");
    }

    @Test
    @DisplayName("같은 엔티티에대해 저장이 여러번 호출되면 예외가 발생하지 않는다.")
    void testPersistManyTimes() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId(null, "john_doe", 30);

        entityManager.persist(entity);
        entityManager.persist(entity);

        EntityManagerTestEntityWithIdentityId persistedEntity = entityManager.find(EntityManagerTestEntityWithIdentityId.class, 1L);
        assertAll(
                () -> assertThat(persistedEntity.id).isEqualTo(1L),
                () -> assertThat(persistedEntity.name).isEqualTo("john_doe"),
                () -> assertThat(persistedEntity.age).isEqualTo(30)
        );
    }

    @Test
    @DisplayName("EntityManager.update()를 통해 엔티티를 수정한다.")
    void testMerge() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId("john_doe", 30);
        entityManager.persist(entity);

        entity.name = "jane_doe";
        entity.age = 40;

        entityManager.merge(entity);

        EntityManagerTestEntityWithIdentityId updated = entityManager.find(EntityManagerTestEntityWithIdentityId.class, 1L);

        assertAll(
                () -> assertThat(updated.id).isEqualTo(1L),
                () -> assertThat(updated.name).isEqualTo("jane_doe"),
                () -> assertThat(updated.age).isEqualTo(40)
        );
    }

    @Test
    @DisplayName("관리되고 있지 않은 엔티티를 EntityManager.merge()를 호출 하면 예외가 발생한다.")
    void testMergeNotManagedEntity() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId(1L, "john_doe", 30);

        entity.name = "jane_doe";
        entity.age = 40;

        IllegalStateException e = assertThrows(IllegalStateException.class, () -> entityManager.merge(entity));
        assertThat(e.getMessage()).isEqualTo("Can not find entity in persistence context: EntityManagerTestEntityWithIdentityId");
    }

    @Test
    @DisplayName("EntityManager.remove()를 통해 엔티티를 삭제한다.")
    void testRemove() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        EntityManagerTestEntityWithIdentityId entity = new EntityManagerTestEntityWithIdentityId(null, "john_doe", 30);
        entityManager.persist(entity);

        entityManager.remove(entity);

        RuntimeException e = assertThrows(
                RuntimeException.class,
                () -> entityManager.find(EntityManagerTestEntityWithIdentityId.class, 1L)
        );
        assertThat(e.getMessage()).isEqualTo("Entity is not managed: EntityManagerTestEntityWithIdentityId");
    }

    @Test
    @DisplayName("Insert 시 연관 테이블이 없으면 Insert되지 않는다.")
    void testInsertWithoutAssociationTable() throws SQLException {

        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        Order order = new Order("order_number");
        entityManager.persist(order);

        Order persistedOrder = entityManager.find(Order.class, 1L);
        assertAll(
                () -> assertThat(persistedOrder.getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderNumber()).isEqualTo("order_number"),
                () -> assertThat(persistedOrder.getOrderItems()).isEmpty()
        );
    }

    @Test
    @DisplayName("Insert 시 연관 테이블까지 Insert 되어야 한다.")
    void testInsertWithAssociationTable() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager em = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        Order order = new Order("order_number");
        OrderItem orderItem1 = new OrderItem("product1", 1);
        OrderItem orderItem2 = new OrderItem("product2", 2);


        order.getOrderItems().add(orderItem1);
        order.getOrderItems().add(orderItem2);

        em.persist(order);
        em.clear();

        Order persistedOrder = em.find(Order.class, 1L);
        assertAll(
                () -> assertThat(persistedOrder.getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderNumber()).isEqualTo("order_number"),
                () -> assertThat(persistedOrder.getOrderItems()).hasSize(2),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getProduct()).isEqualTo("product1"),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(1),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getId()).isEqualTo(2L),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getProduct()).isEqualTo("product2"),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getQuantity()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("연관 데이터가 없다면 조회 시 빈 리스트를 반환한다.")
    void testFindWithoutAssociationTable() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        Order order = new Order("order_number");
        entityManager.persist(order);

        Order persistedOrder = entityManager.find(Order.class, 1L);
        assertAll(
                () -> assertThat(persistedOrder.getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderNumber()).isEqualTo("order_number"),
                () -> assertThat(persistedOrder.getOrderItems()).isEmpty()
        );
    }

    @Test
    @DisplayName("Eager Fetch 전략을 사용하여 Join을 통해 데이터를 조회한다.")
    void testEagerFetch() throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        TestEagerOrder order = new TestEagerOrder("order_number");
        TestEagerOrderItem orderItem1 = new TestEagerOrderItem("product1", 1);
        TestEagerOrderItem orderItem2 = new TestEagerOrderItem("product2", 2);

        order.getOrderItems().add(orderItem1);
        order.getOrderItems().add(orderItem2);

        entityManager.persist(order);
        entityManager.clear();

        TestEagerOrder persistedOrder = entityManager.find(TestEagerOrder.class, 1L);
        System.out.println("-------------------------After Find----------------------------------");
        assertAll(
                () -> assertThat(persistedOrder.getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderNumber()).isEqualTo("order_number"),
                () -> assertThat(persistedOrder.getOrderItems()).hasSize(2),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getProduct()).isEqualTo("product1"),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(1),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getId()).isEqualTo(2L),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getProduct()).isEqualTo("product2"),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getQuantity()).isEqualTo(2)
        );
    }

    @Test
    @DisplayName("Lazy Fetch 전략을 사용하여 데이터가 접근 될 때 쿼리가 발생한다.")
    void testLazyFetch() throws SQLException {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(server.getConnection());
        EntityManager entityManager = new EntityManagerImpl(jdbcTemplate, new PersistenceContextImpl(), new EntityPersister(jdbcTemplate));
        TestLazyOrder order = new TestLazyOrder("order_number");
        TestLazyOrderItem orderItem1 = new TestLazyOrderItem("product1", 1);
        TestLazyOrderItem orderItem2 = new TestLazyOrderItem("product2", 2);

        order.getOrderItems().add(orderItem1);
        order.getOrderItems().add(orderItem2);

        entityManager.persist(order);
        entityManager.clear();

        TestLazyOrder persistedOrder = entityManager.find(TestLazyOrder.class, 1L);

        assertAll(
                () -> assertThat(Proxy.isProxyClass(persistedOrder.getOrderItems().getClass())).isTrue(),
                () -> assertThat(Proxy.getInvocationHandler(persistedOrder.getOrderItems())).isInstanceOf(PersistentList.class),
                () -> assertThat(((PersistentList<?>) Proxy.getInvocationHandler(persistedOrder.getOrderItems())).isInitialized()).isFalse(),

                () -> assertThat(persistedOrder.getOrderItems()).hasSize(2),

                () -> assertThat(((PersistentList<?>) Proxy.getInvocationHandler(persistedOrder.getOrderItems())).isInitialized()).isTrue(),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getId()).isEqualTo(1L),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getProduct()).isEqualTo("product1"),
                () -> assertThat(persistedOrder.getOrderItems().get(0).getQuantity()).isEqualTo(1),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getId()).isEqualTo(2L),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getProduct()).isEqualTo("product2"),
                () -> assertThat(persistedOrder.getOrderItems().get(1).getQuantity()).isEqualTo(2)
        );
    }
}
