package orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;

import static org.assertj.core.api.Assertions.assertThat;

class TableFieldsTest {

    @Test
    @DisplayName("""
            TableFields#getAllFields는 연관관계가 아닌 필드를 모두 가져온다.
            - TableFields 는 연관관계가 아닌 필드만을 가지고 있다.
            """)
    void TableFields_AllFields_테스트() {
        // given
        Order order = new Order("12131");

        // when
        var tableFields = new TableFields<>(order);

        // then
        assertThat(tableFields.getAllFields())
                .hasSize(2); // 연관관계 아닌 필드 2
    }

    @Test
    @DisplayName("""
            TableFields#getNonIdFields는 연관관계가 아니면서 아이디도 아닌 필드를 모두 가져온다.
            """)
    void TableFields_NonIdFields_테스트() {
        // given
        Order order = new Order("12131");

        // when
        var tableFields = new TableFields<>(order);

        // then
        assertThat(tableFields.getNonIdFields())
                .hasSize(1); // 연관관계 아닌 필드 2
    }
}
