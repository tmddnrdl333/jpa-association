package persistence.sql.dml;

import domain.Order;
import domain.OrderItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixture.EntityWithId;
import persistence.fixture.EntityWithoutID;
import persistence.meta.EntityColumns;

import static org.assertj.core.api.Assertions.*;

class SelectQueryTest {
    @Test
    @DisplayName("findAll 쿼리를 생성한다.")
    void findAll() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when
        final String sql = selectQuery.findAll(EntityWithId.class);

        // then
        assertThat(sql).isEqualTo("SELECT id, nick_name, old, email FROM users");
    }

    @Test
    @DisplayName("연관관계가 존재하는 엔티티의 findAll 쿼리를 생성한다.")
    void findAll_withAssociation() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when
        final String sql = selectQuery.findAll(Order.class);

        // then
        assertThat(sql).isEqualTo("SELECT _orders.id, _orders.orderNumber, _order_items.id, "
                + "_order_items.product, _order_items.quantity FROM orders _orders "
                + "INNER JOIN order_items _order_items ON _orders.id = _order_items.order_id");
    }

    @Test
    @DisplayName("findById 쿼리를 생성한다.")
    void findById() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when
        final String sql = selectQuery.findById(EntityWithId.class, 1);

        // then
        assertThat(sql).isEqualTo("SELECT id, nick_name, old, email FROM users WHERE id = 1");
    }

    @Test
    @DisplayName("연관관계가 존재하는 엔티티의 findById 쿼리를 생성한다.")
    void findById_withAssociation() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when
        final String sql = selectQuery.findById(Order.class, 1);

        // then
        assertThat(sql).isEqualTo("SELECT _orders.id, _orders.orderNumber, _order_items.id, "
                + "_order_items.product, _order_items.quantity FROM orders _orders "
                + "INNER JOIN order_items _order_items ON _orders.id = _order_items.order_id "
                + "WHERE _orders.id = 1");
    }

    @Test
    @DisplayName("findCollection 쿼리를 생성한다.")
    void findCollection() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when
        final String sql = selectQuery.findCollection(OrderItem.class, "order_id", 1);

        // then
        assertThat(sql).isEqualTo("SELECT id, product, quantity FROM order_items WHERE order_id = 1");
    }

    @Test
    @DisplayName("@Id가 없는 엔티티로 findById 쿼리를 생성하면 예외를 발생한다.")
    void findById_exception() {
        // given
        final SelectQuery selectQuery = new SelectQuery();

        // when & then
        assertThatThrownBy(() -> selectQuery.findById(EntityWithoutID.class, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(EntityColumns.NOT_ID_FAILED_MESSAGE);
    }
}
