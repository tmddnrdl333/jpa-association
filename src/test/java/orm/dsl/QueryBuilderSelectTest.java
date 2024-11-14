package orm.dsl;

import config.PluggableH2test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;
import persistence.sql.ddl.Person;
import test_double.FakeQueryRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static orm.dsl.DSL.eq;
import static steps.Steps.*;
import static util.SQLUtil.SQL_노멀라이즈;

class QueryBuilderSelectTest extends PluggableH2test {

    QueryBuilder queryBuilder;
    QueryRunner fakeQueryRunner;

    @BeforeEach
    void setUp() {
        queryBuilder = new QueryBuilder();
        fakeQueryRunner = new FakeQueryRunner();
    }

    @Test
    @DisplayName("SELECT 절 생성 테스트")
    void DQL_SELECT_문_테스트() {
        // when
        String query = queryBuilder.selectFrom(Person.class, fakeQueryRunner)
                .extractSql();

        // then
        assertThat(query).isEqualTo("SELECT id,name,age FROM person");
    }

    @Test
    @DisplayName("SELECT 절 조건 포함 실행 테스트")
    void DQL_SELECT_문_WHERE_포함_테스트() {
        // when
        String query = queryBuilder.selectFrom(Person.class, fakeQueryRunner)
                .where(eq("id", 1L))
                .extractSql();

        // then
        assertThat(query).isEqualTo("SELECT id,name,age FROM person WHERE id = 1");
    }

    @Test
    @DisplayName("SELECT 절 다중 조건 포함 실행 테스트 - 가변인자는 AND 고정이다.")
    void DQL_SELECT_문_WHERE_다중_포함_테스트_가변인자() {

        // when
        String query = queryBuilder.selectFrom(Person.class, fakeQueryRunner)
                .where(
                    eq("id", 1L),
                    eq("name", "설동민")
                )
                .extractSql();

        // then
        assertThat(query).isEqualTo("SELECT id,name,age FROM person WHERE id = 1 AND name = '설동민'");
    }

    @Test
    @DisplayName("SELECT 절 다중 조건 포함 실행 테스트 - 체이닝된 조건절을 만들어보자")
    void DQL_SELECT_문_WHERE_다중_포함_테스트_AND() {
        // when
        String query = queryBuilder.selectFrom(Person.class, fakeQueryRunner)
                .where(
                        eq("id", 1L)
                        .and(eq("name", "설동민"))
                        .or(eq("age", 30))
                )
                .extractSql();

        // then
        assertThat(query).isEqualTo("SELECT id,name,age FROM person WHERE id = 1 AND name = '설동민' OR age = 30");
    }

    @Test
    @DisplayName("SELECT 절 실제 쿼리 실행 테스트")
    void DQL_SELECT_실제_쿼리_실행() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            Person newPerson = new Person(1L, 30, "설동민");
            테이블_생성(queryRunner, Person.class);
            Person_엔티티_생성(queryRunner, newPerson);

            // when
            Person person = queryBuilder.selectFrom(Person.class, queryRunner)
                    .where(eq("id", 1L))
                    .fetchOne();

            // then
            assertThat(person).hasNoNullFieldsOrPropertiesExcept("id", "name", "age");
        });
    }

    @Test
    @DisplayName("SELECT 절 실제 쿼리 실행 테스트 - findById()")
    void findById_실헹() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            Person newPerson = new Person(1L, 30, "설동민");
            테이블_생성(queryRunner, Person.class);
            Person_엔티티_생성(queryRunner, newPerson);
            // when
            Person person = queryBuilder.selectFrom(Person.class, queryRunner).findById(1L)
                    .fetchOne();

            // then
            assertThat(person).hasNoNullFieldsOrPropertiesExcept("id", "name", "age")
                    .extracting("id").isEqualTo(1L);
        });
    }

    @Test
    @DisplayName("SELECT 절 실제 쿼리 실행 테스트 - findByAll()")
    void findAll_실행() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Person.class);
            Person_엔티티_생성(queryRunner, new Person(1L, 30, "설동민"));
            Person_엔티티_생성(queryRunner, new Person(2L, 30, "설동민2"));

            // when
            List<Person> people = queryBuilder.selectFrom(Person.class, queryRunner).findAll()
                    .fetch();

            // then
            assertThat(people).asList()
                    .hasSize(2)
                    .allSatisfy(person -> assertThat(person).hasNoNullFieldsOrPropertiesExcept("id", "name", "age"));
        });
    }

    @Test
    @DisplayName("JOIN 절이 포함된 SELECT 절 쿼리빌더 테스트")
    void select_join_절_생성_테스트() {

        // given
        var queryStep = queryBuilder.selectFrom(Order.class, fakeQueryRunner)
                .joinAll()
                .whereWithId(1L);

        // 정규식 패턴 생성 (랜덤으로 생성되는 alias는 정규식으로 처리한다.)
        String 예상결과_정규식 = SQL_노멀라이즈("""
        SELECT [order].id AS [order]_id,
               [order].order_number AS [order]_order_number,
               [order_items].id AS [order_items]_id,
               [order_items].product AS [order_items]_product,
               [order_items].quantity AS [order_items]_quantity,
               [order_items].order_id AS [order_items]_order_id
         FROM orders [order]
         JOIN order_items [order_items] ON [order].id = [order_items].order_id
         WHERE [order].id = 1
        """.replaceAll("\\[order]", "orders_\\\\d{1,3}")
           .replaceAll("\\[order_items]", "order_items_\\\\d{1,3}"));

        // when
        String query = SQL_노멀라이즈(queryStep.extractSql());

        // then
        assertThat(query).matches(예상결과_정규식);
    }

    @Test
    @DisplayName("""
        JOIN 절이 포함되지 않은 SELECT 절 쿼리빌더 테스트
        - JOIN 절이 없으면, alias를 생성하지 않는다.
    """)
    void select_절_생성_테스트() {

        // given
        var queryStep = queryBuilder.selectFrom(Order.class, fakeQueryRunner)
                .whereWithId(1L);

        String 예상결과 = SQL_노멀라이즈("""
        SELECT id,
               order_number
         FROM orders
         WHERE id = 1
        """);

        // when
        String query = SQL_노멀라이즈(queryStep.extractSql());

        // then
        assertThat(query).isEqualTo(예상결과);
    }

    @Test
    @DisplayName("JOIN 절이 포함된 SELECT 절 쿼리 실행 테스트")
    void select_join_절_실행_테스트() {

        runInH2Db((queryRunner, queryBuilder) -> {

            // given
            테이블_생성(queryRunner, Order.class);
            테이블_생성(queryRunner, OrderItem.class);

            Order order = Order_엔티티_생성(queryRunner, new Order("12131"));
            OrderItem orderItem1 = OrderItem_엔티티_생성(queryRunner, new OrderItem(order.getId(), "product1", 10));
            OrderItem orderItem2 = OrderItem_엔티티_생성(queryRunner, new OrderItem(order.getId(), "product2", 11));

            // when
            Order result = queryBuilder.selectFrom(Order.class, queryRunner)
                    .joinAll()
                    .whereWithId(1L)
                    .fetchOne();

            // then
            assertThat(result)
                    .satisfies(order1 -> {
                        assertThat(order1).hasNoNullFieldsOrPropertiesExcept("id", "orderNumber", "orderItems");
                        assertThat(order1.getOrderItems()).asList()
                                .hasSize(2)
                                .allSatisfy(orderItem -> assertThat(orderItem).hasNoNullFieldsOrPropertiesExcept("id", "product", "quantity", "orderId"));
                    });

        });
    }
}
