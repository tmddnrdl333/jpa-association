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
        assertThat(leftJoinClause.clause()).isEqualTo("LEFT JOIN order_items testorderitem ON testorder.id = testorderitem.id");
        assertThat(leftJoinClause.columns()).isEqualTo("testorderitem.id, testorderitem.product, testorderitem.quantity");
    }
}
