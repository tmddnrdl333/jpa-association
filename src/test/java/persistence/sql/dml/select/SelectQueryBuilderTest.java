package persistence.sql.dml.select;

import example.entity.Order;
import example.entity.OrderItem;
import example.entity.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.sql.component.ColumnInfo;
import persistence.sql.component.ConditionBuilder;
import persistence.sql.component.JoinConditionBuilder;
import persistence.sql.component.JoinType;
import persistence.sql.component.TableInfo;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectQueryBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(SelectQueryBuilderTest.class);

    @Test
    @DisplayName("FindAll query 테스트")
    void findAllQueryTest() {
        Class<Person> personClass = Person.class;
        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(TableInfo.from(personClass))
                .build();
        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from users;");
    }

    @Test
    @DisplayName("FindById query 테스트")
    void findByIdTest() {
        Class<Person> personClass = Person.class;
        TableInfo personTable = TableInfo.from(personClass);
        ColumnInfo idColumn = personTable.getIdColumn();

        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(personTable)
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(idColumn)
                                .values(Collections.singletonList("1"))
                                .build()
                )
                .build();
        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from users where users.id = 1;");
    }

    @Test
    @DisplayName("Find with join test")
    void findWithJoinTest() {
        Class<Order> orderClass = Order.class;
        Class<OrderItem> orderItemClass = OrderItem.class;

        TableInfo orderTableInfo = TableInfo.from(orderClass);
        TableInfo orderItemTableInfo = TableInfo.from(orderItemClass);

        ColumnInfo orderId = orderTableInfo.getIdColumn();
        ColumnInfo orderOrderId = orderTableInfo.getColumn("order_id");
        ColumnInfo orderItemId = orderItemTableInfo.getIdColumn();

        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(orderTableInfo)
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(orderId)
                                .values(Collections.singletonList("1"))
                                .build()
                )
                .joinConditions(
                        Collections.singletonList(
                                new JoinConditionBuilder()
                                        .joinType(JoinType.INNER_JOIN)
                                        .tableInfo(orderItemTableInfo)
                                        .sourceColumnInfo(orderOrderId)
                                        .targetColumnInfo(orderItemId)
                                        .build()
                        )
                )
                .build();

        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from orders where orders.id = 1 inner join order_items on orders.order_id = order_items.id;");
    }
}
