package orm;

import config.PluggableH2test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;
import persistence.sql.ddl.Person;
import test_entity.PersonWithAI;

import static org.assertj.core.api.Assertions.assertThat;
import static steps.Steps.*;

class SessionImplTest extends PluggableH2test {

    @Test
    @DisplayName("insert 후 find 메서드를 사용하면 엔티티 Object의 Equality와 Identity 모두 유지된다.")
    void find_테스트() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Person.class);
            SessionImpl session = new SessionImpl(queryRunner);

            Person newPerson = new Person(1L, 30, "설동민");
            session.persist(newPerson);

            // when
            Person person = session.find(Person.class, 1L);

            // then
            assertThat(person)
                    .isSameAs(newPerson)
                    .satisfies(p -> { // Equality 검증
                        assertThat(p.getId()).isEqualTo(1L);
                        assertThat(p.getAge()).isEqualTo(30);
                        assertThat(p.getName()).isEqualTo("설동민");
                    });
        });
    }

    @Test
    @DisplayName("persist 메서드를 사용하면 엔티티 Object의 Equality와 Identity가 모두 유지된다.")
    void persistence_테스트() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Person.class);
            SessionImpl session = new SessionImpl(queryRunner);
            Person newPerson = new Person(1L, 30, "설동민");

            // when
            Person person = session.persist(newPerson);

            // then
            assertThat(person)
                    .isSameAs(newPerson) // Identity 검증
                    .satisfies(p -> { // Equality 검증
                        assertThat(p.getId()).isEqualTo(1L);
                        assertThat(p.getAge()).isEqualTo(30);
                        assertThat(p.getName()).isEqualTo("설동민");
                    });
        });
    }

    @Test
    @DisplayName("auto-increment 키를 가진 엔티티를 persist 하면, db애서 채번된 auto-increment 값을 id 필드에 세팅한다.")
    void persistence_auto_increment_테스트() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, PersonWithAI.class);

            SessionImpl session = new SessionImpl(queryRunner);

            // when
            PersonWithAI person = session.persist(new PersonWithAI(30L, "설동민"));

            // then
            assertThat(person)
                    .satisfies(p -> { // Equality 검증
                        assertThat(p.getId()).isEqualTo(1L);
                        assertThat(p.getAge()).isEqualTo(30);
                        assertThat(p.getName()).isEqualTo("설동민");
                    });
        });
    }

    @Test
    @DisplayName("delete 후 find 메서드를 사용하면 엔티티 결과는 null이 리턴된다.")
    void delete_테스트() {
        runInH2Db((queryRunner, queryBuilder) -> {

            // given
            테이블_생성(queryRunner, Person.class);

            SessionImpl session = new SessionImpl(queryRunner);
            session.persist(new Person(1L, 30, "설동민"));
            Person person = session.find(Person.class, 1L);

            // when
            session.remove(person);
            Person result = session.find(Person.class, 1L);

            // then
            assertThat(result).isNull();
        });
    }

    @Test
    @DisplayName("엔티티 수정, merge 이후 엔티티를 재조회하면 수정 전, merge 후, 재조회한 객체의 identity가 모두 같아야 한다.")
    void merge_후_재조회_테스트() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Person.class);
            SessionImpl session = new SessionImpl(queryRunner);
            session.persist(new Person(1L, 30, "설동민"));

            Person person = session.find(Person.class, 1L);
            person.setName("설동민 - 수정함");
            person.setAge(20);

            // when
            Person mergedPerson = session.merge(person);
            Person foundPerson = session.find(Person.class, 1L);

            // then
            assertThat(mergedPerson)
                    .isSameAs(person) // merge 되기 전의 엔티티와 identity가 같아야 한다.
                    .isSameAs(foundPerson); // merge 후 다시 조회한 엔티티와도 identity가 같아야 한다.
        });
    }

    @Test
    @DisplayName("신규 엔티티를 merge 하면 insert 되어 PK가 존재해야한다.")
    void 신규_엔티티_merge() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, PersonWithAI.class);
            SessionImpl session = new SessionImpl(queryRunner);

            PersonWithAI personWithAI = new PersonWithAI(30L, "설동민");

            // when
            session.merge(personWithAI);

            // then
            assertThat(personWithAI.getId()).isNotNull();
        });
    }

    @Test
    @DisplayName("엔티티를 조회하면 Eager 연관관계를 포함해서 조회한다.")
    void 엔티티_Eager_포함_조회() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Order.class);
            테이블_생성(queryRunner, OrderItem.class);
            SessionImpl session = new SessionImpl(queryRunner);

            Order order = Order_엔티티_생성(queryRunner, new Order("12131"));
            OrderItem orderItem1 = OrderItem_엔티티_생성(queryRunner, new OrderItem(order.getId(), "product1", 10));
            OrderItem orderItem2 = OrderItem_엔티티_생성(queryRunner, new OrderItem(order.getId(), "product2", 11));

            // when
            Order foundOrder = session.find(Order.class, 1L);

            // then
            assertThat(foundOrder)
                    .satisfies(order1 -> {
                        assertThat(order1).hasNoNullFieldsOrPropertiesExcept("id", "orderNumber", "orderItems");
                        assertThat(order1.getOrderItems()).asList()
                                .hasSize(2)
                                .allSatisfy(orderItem -> assertThat(orderItem).hasNoNullFieldsOrPropertiesExcept("id", "product", "quantity", "orderId"));
                    });
        });
    }

    @Test
    @DisplayName("연관관계가 포함된 엔티티를 저장하면 연관관계도 같이 저장한다.")
    void 엔티티_Eager_포함_저장() {
        runInH2Db((queryRunner, queryBuilder) -> {
            // given
            테이블_생성(queryRunner, Order.class);
            테이블_생성(queryRunner, OrderItem.class);
            SessionImpl session = new SessionImpl(queryRunner);

            Order order = new Order("12131");
            order.addOrderItem(new OrderItem("product1", 10));
            order.addOrderItem(new OrderItem("product2", 11));

            // when
            session.persist(order);
            Order foundOrder = session.find(Order.class, 1L);

            // then
            assertThat(foundOrder)
                    .satisfies(order1 -> {
                        assertThat(order1).hasNoNullFieldsOrProperties();
                        assertThat(order1.getOrderItems()).asList()
                                .hasSize(2)
                                .allSatisfy(orderItem -> assertThat(orderItem).hasNoNullFieldsOrPropertiesExcept("orderId")); // orderId는 저장하지 않음
                    });
        });
    }
}
