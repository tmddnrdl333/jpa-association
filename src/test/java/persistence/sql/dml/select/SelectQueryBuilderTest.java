package persistence.sql.dml.select;

import example.entity.Order;
import example.entity.OrderItem;
import example.entity.Person;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.entity.EntityUtils;
import persistence.sql.component.ColumnInfo;
import persistence.sql.component.ConditionBuilder;
import persistence.sql.component.JoinConditionBuilder;
import persistence.sql.component.JoinType;
import persistence.sql.component.TableInfo;

import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class SelectQueryBuilderTest {
    private static final Logger logger = LoggerFactory.getLogger(SelectQueryBuilderTest.class);

    @Test
    @DisplayName("FindAll query 테스트")
    void findAllQueryTest() {
        Class<Person> personClass = Person.class;
        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(new TableInfo(personClass))
                .build();
        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from users;");
    }

    @Test
    @DisplayName("FindById query 테스트")
    void findByIdTest() {
        Class<Person> personClass = Person.class;
        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(new TableInfo(personClass))
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(EntityUtils.getIdColumn(personClass))
                                .values(Collections.singletonList("1"))
                                .build()
                )
                .build();
        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from users where id = 1;");
    }

    @Test
    @DisplayName("Find with join test")
    void findWithJoinTest() {
        Class<Order> orderClass = Order.class;
        Class<OrderItem> orderItemClass = OrderItem.class;

        TableInfo orderTableInfo = new TableInfo(orderClass);
        TableInfo orderItemTableInfo = new TableInfo(orderItemClass);

        ColumnInfo orderDotOrderId = new ColumnInfo(
                orderTableInfo,
                Arrays.stream(orderClass.getDeclaredFields())
                        .filter(field -> field.getName().equals("orderItems"))
                        .findAny()
                        .get()
        );
        ColumnInfo orderItemDotId = EntityUtils.getIdColumn(orderItemClass);

        SelectQuery selectQuery = new SelectQueryBuilder()
                .fromTableInfo(orderTableInfo)
                .whereCondition(
                        new ConditionBuilder()
                                .columnInfo(EntityUtils.getIdColumn(orderClass))
                                .values(Collections.singletonList("1"))
                                .build()
                )
                .joinConditions(
                        Collections.singletonList(
                                new JoinConditionBuilder()
                                        .joinType(JoinType.INNER_JOIN)
                                        .tableInfo(orderItemTableInfo)
                                        .onConditionColumn1(orderDotOrderId)
                                        .onConditionColumn2(orderItemDotId)
                                        .build()
                        )
                )
                .build();

        String query = selectQuery.toString();
        logger.debug(query);
        assertThat(query).isEqualTo("select * from orders where id = 1 inner join order_items on orders.order_id = order_items.id;");
    }
}
