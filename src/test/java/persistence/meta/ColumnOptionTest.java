package persistence.meta;

import domain.Order;
import domain.OrderItem;
import jakarta.persistence.FetchType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixture.EntityWithId;
import persistence.meta.ColumnOption;
import util.ReflectionUtils;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class ColumnOptionTest {
    @Test
    @DisplayName("@Column 애노테이션의 nullable 속성이 true인 필드로 인스턴스를 생성한다.")
    void constructor_withNullableTrue() {
        // given
        final Field field = ReflectionUtils.getField(EntityWithId.class, "name");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.isNotNull()).isFalse();
    }

    @Test
    @DisplayName("@Column 애노테이션의 nullable 속성이 false인 필드로 인스턴스를 생성한다.")
    void constructor_withNullableFalse() {
        // given
        final Field field = ReflectionUtils.getField(EntityWithId.class, "email");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.isNotNull()).isTrue();
    }

    @Test
    @DisplayName("@OneToMany 애노테이션이 존재하는 필드로 인스턴스를 생성한다.")
    void constructor_withOneToMany() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "orderItems");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.isOneToManyAssociation()).isTrue();
    }

    @Test
    @DisplayName("@OneToMany 애노테이션이 존재하지 않는 필드로 인스턴스를 생성한다.")
    void constructor_withoutOneToMany() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "id");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.isOneToManyAssociation()).isFalse();
    }

    @Test
    @DisplayName("@OneToMany 애노테이션의 FetchType 속성이 존재하는 필드로 인스턴스를 생성한다.")
    void constructor_withFetchType() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "orderItems");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.getFetchType()).isEqualTo(FetchType.EAGER);
    }

    @Test
    @DisplayName("@OneToMany 애노테이션의 FetchType 속성이 존재하지 않는 필드로 인스턴스를 생성한다.")
    void constructor_withoutFetchType() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "id");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertThat(columnOption.getFetchType()).isNull();
    }

    @Test
    @DisplayName("@JoinColumn 애노테이션이 존재하는 필드로 인스턴스를 생성한다.")
    void constructor_withJoinColumn() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "orderItems");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertAll(
                () -> assertThat(columnOption.getJoinColumnName()).isEqualTo("order_id"),
                () -> assertThat(columnOption.getJoinColumnType()).isEqualTo(OrderItem.class)
        );

    }

    @Test
    @DisplayName("@JoinColumn 애노테이션이 존재하지 않는 필드로 인스턴스를 생성한다.")
    void constructor_withoutJoinColumn() {
        // given
        final Field field = ReflectionUtils.getField(Order.class, "id");

        // when
        final ColumnOption columnOption = new ColumnOption(field);

        // then
        assertAll(
                () -> assertThat(columnOption.getJoinColumnName()).isNull(),
                () -> assertThat(columnOption.getJoinColumnType()).isNull()
        );
    }
}
