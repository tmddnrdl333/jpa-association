package persistence.sql.clause;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.fixture.TestOrder;
import persistence.sql.fixture.TestOrderItem;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LeftJoinClause 테스트")
class LeftJoinClauseTest {


    @Test
    @DisplayName("of 함수를 통해 leftJoinClause 객체를 생성할 수 있다.")
    public void of() {
        // given

        // when
        LeftJoinClause leftJoinClause = LeftJoinClause.of(TestOrder.class, TestOrderItem.class);

        // then
        assertThat(leftJoinClause.clause()).isEqualTo("LEFT JOIN order_items order_items ON orders.id = order_items.order_id");
        assertThat(leftJoinClause.columns()).isEqualTo("order_items.id, order_items.product, order_items.quantity");
    }
}
