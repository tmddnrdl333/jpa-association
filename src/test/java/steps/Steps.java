package steps;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;
import persistence.sql.ddl.Person;

public class Steps {

    public static <T> void 테이블_생성(QueryRunner queryRunner, Class<T> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.createTable(entityClass, queryRunner)
                .execute();
    }

    public static void Person_엔티티_생성(QueryRunner queryRunner, Person person) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.insertInto(Person.class, queryRunner)
                .value(person)
                .returnAsEntity();

    }

    public static Order Order_엔티티_생성(QueryRunner queryRunner, Order order) {
        QueryBuilder queryBuilder = new QueryBuilder();
        return queryBuilder.insertInto(Order.class, queryRunner)
                .value(order)
                .returnAsEntity();

    }

    public static OrderItem OrderItem_엔티티_생성(QueryRunner queryRunner, OrderItem orderItem) {
        QueryBuilder queryBuilder = new QueryBuilder();
        return queryBuilder.insertInto(OrderItem.class, queryRunner)
                .value(orderItem)
                .returnAsEntity();
    }
}
