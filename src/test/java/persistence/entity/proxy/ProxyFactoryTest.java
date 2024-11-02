package persistence.entity.proxy;

import database.H2ConnectionFactory;
import domain.Order;
import domain.OrderItem;
import domain.OrderLazy;
import jdbc.JdbcTemplate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.entity.DefaultEntityLoader;
import persistence.entity.DefaultEntityPersister;
import persistence.entity.EntityLoader;
import persistence.entity.EntityPersister;
import persistence.sql.dml.DeleteQuery;
import persistence.sql.dml.InsertQuery;
import persistence.sql.dml.SelectQuery;
import persistence.sql.dml.UpdateQuery;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static util.QueryUtils.*;

class ProxyFactoryTest {
    private final JdbcTemplate jdbcTemplate = new JdbcTemplate(H2ConnectionFactory.getConnection());
    private final Order order = new Order("OrderNumber1");
    private final OrderItem orderItem1 = new OrderItem("Product1", 10);
    private final OrderItem orderItem2 = new OrderItem("Product2", 20);

    @BeforeEach
    void setUp() {
        createTable(OrderLazy.class);
        createTable(OrderItem.class, OrderLazy.class);

        final EntityPersister entityPersister = new DefaultEntityPersister(
                jdbcTemplate, new InsertQuery(), new UpdateQuery(), new DeleteQuery());

        entityPersister.insert(order);
        order.addOrderItem(orderItem1);
        entityPersister.insert(orderItem1, order);
        order.addOrderItem(orderItem2);
        entityPersister.insert(orderItem2, order);
    }

    @AfterEach
    void tearDown() {
        dropTable(OrderLazy.class);
        dropTable(OrderItem.class);
    }

    @Test
    @DisplayName("프록시 생성 후 컬렉션에 접근하면 lazy 로딩 된다.")
    void createProxyAndLazyLoading() {
        // given
        final ProxyFactory proxyFactory = new ProxyFactory();
        final EntityLoader entityLoader = new DefaultEntityLoader(jdbcTemplate, new SelectQuery(), new ProxyFactory());
        final OrderLazy managedOrder = entityLoader.load(OrderLazy.class, order.getId());

        // when
        final List<OrderItem> proxy = proxyFactory.createProxy(entityLoader, OrderItem.class, managedOrder);
        proxy.size();

        // then
        assertAll(
                () -> assertThat(proxy).hasSize(2),
                () -> assertThat(proxy).containsExactly(orderItem1, orderItem2)
        );
    }
}
