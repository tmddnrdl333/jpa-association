package orm.assosiation;

import jakarta.persistence.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Order;
import persistence.sql.ddl.OrderItem;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.*;

class RelationFieldsTest {

    @Test
    @DisplayName("RelationFields#getRelationList 는 연관관계 필드들을 리턴한다.")
    void getRelationList() {
        // given
        Order order = new Order("12131");

        // when
        var relationFields = new RelationFields<>(order);

        // then
        assertThat(relationFields.getRelationList())
                .hasSize(1); // oneToMany 필드 1
    }

    @Test
    @DisplayName("RelationFields#getValuedRelationList 는 연관관계 필드중 값이 있는 필드들을 리턴한다.")
    void getValuedRelationList() {
        // given
        Order order = new Order("12131");
        order.addOrderItem(new OrderItem("product1", 10));
        order.addOrderItem(new OrderItem("product2", 11));

        Order singleOrder = new Order("12132");

        var relationFields = new RelationFields<>(order);
        var relationFields2 = new RelationFields<>(singleOrder);

        // when
        var result = relationFields.getValuedRelationList();
        var result2 = relationFields2.getValuedRelationList();

        // then
        assertThat(result).hasSize(1); // orderItems 필드 하나에만 연관관계 값 존재
        assertThat(result2).hasSize(0);
    }

    @Test
    @DisplayName("RelationFields#getRelationFieldsOfType 는 특정 타입의 필드들을 리턴한다.")
    void getRelationFieldsOfType() {
        // given
        Order order = new Order("12131");

        // when
        var relationFields = new RelationFields<>(order);

        // then
        assertThat(relationFields.getRelationFieldsOfType(OrderItem.class)).isNotNull(); // OrderItem 필드 1
    }

    @Test
    @DisplayName("RelationFields#getEagerRelationList 는 Eager 로딩이 필요한 필드들을 리턴한다.")
    void getEagerRelationList() {
        // given
        Order order = new Order("12131");

        // when
        var relationFields = new RelationFields<>(order);

        // then
        assertThat(relationFields.getEagerRelationList())
                .isNotEmpty(); // oneToMany, EAGER 필드 1
    }

    @Test
    @DisplayName("RelationFields#hasRelation 는 연관관계 필드가 있는지 확인한다.")
    void hasRelation() {
        // given
        Order order = new Order("12131");

        // when
        var relationFields = new RelationFields<>(order);

        // then
        assertTrue(relationFields.hasRelation());
    }

    @Test
    @DisplayName("RelationFields는 Join 컬럼명이 없는 경우 연관관계 테이블의 Id를 사용한다.")
    void join컬럼_명시안된_필드_테스트() {
        // given
        var order = new Order_조인컬럼_없는_경우();

        // when
        var relationFields = new RelationFields<>(order);

        // then
        List<RelationField> relationList = relationFields.getRelationList();
        assertSoftly(softly -> {
            assertThat(relationList).hasSize(1);
            assertThat(relationList.getFirst().getJoinColumnName()).isEqualTo("id");
        });
    }
}


@Entity
@Table(name = "orders_x")
class Order_조인컬럼_없는_경우{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "")
    public List<OrderItem> orderItems;

    public Order_조인컬럼_없는_경우() {

    }
}


