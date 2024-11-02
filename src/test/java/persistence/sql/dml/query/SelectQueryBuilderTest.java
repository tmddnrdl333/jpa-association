package persistence.sql.dml.query;

import domain.Order;
import domain.OrderItem;
import org.junit.jupiter.api.Test;
import persistence.sql.definition.TableDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class SelectQueryBuilderTest {

    @Test
    void testSelectSingleTable() {
        Order order = new Order("order_number");
        String selectQuery = new SelectQueryBuilder(order.getClass()).buildById(1);
        assertThat(selectQuery).isEqualTo(
                "SELECT orders.order_id AS orders_order_id, orders.orderNumber AS orders_orderNumber " +
                        "FROM orders WHERE orders.order_id = 1;");
    }

    @Test
    void testSelectSingleTableWithJoin() {
        Order order = new Order("order_number");
        TableDefinition orderTableDefinition = new TableDefinition(order.getClass());

        SelectQueryBuilder selectQuery = new SelectQueryBuilder(order.getClass());
        orderTableDefinition.getAssociations().forEach(association -> {
            selectQuery.join(association);
        });

        assertThat(selectQuery.buildById(1)).isEqualTo(
                "SELECT " +
                        "orders.order_id AS orders_order_id, orders.orderNumber AS orders_orderNumber, " +
                        "order_items.id AS order_items_id, order_items.product AS order_items_product, order_items.quantity AS order_items_quantity " +
                        "FROM orders " +
                        "LEFT JOIN order_items " +
                        "ON order_items.order_id = orders.order_id " +
                        "WHERE orders.order_id = 1;");
    }

    @Test
    void testFindAll() {
        Order order = new Order(1L, "order_number");
        TableDefinition orderTableDefinition = new TableDefinition(order.getClass());
        String joinColumnName = orderTableDefinition.getJoinColumnName(OrderItem.class);
        Object value = orderTableDefinition.getValue(order, joinColumnName);

        OrderItem orderItem = new OrderItem("product", 1);
        String selectQuery = new SelectQueryBuilder(orderItem.getClass()).where(joinColumnName, value.toString()).build();
        assertThat(selectQuery).isEqualTo(
                "SELECT order_items.id AS order_items_id, order_items.product AS order_items_product, order_items.quantity AS order_items_quantity " +
                        "FROM order_items " +
                        "WHERE order_items.order_id = '1'");
    }
}
