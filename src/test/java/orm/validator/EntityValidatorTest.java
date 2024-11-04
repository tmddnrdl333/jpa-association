package orm.validator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.TableEntity;
import orm.exception.InvalidEntityException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class EntityValidatorTest {

    @Test
    @DisplayName("@Entity가 아닌 클래스는 허용하지 않는다.")
    void 엔티티_애너테이션_검증() {
        // given
        Class<DummyClass> tableClass = DummyClass.class;
        var validator = new EntityValidator<>(tableClass);

        // when && then
        assertThatThrownBy(validator::validate)
                .isInstanceOf(InvalidEntityException.class)
                .hasMessageContaining("is not an entity");
    }

    @Test
    @DisplayName("하나의 필드에 @Column 과 @Transient이 동시에 사용 될 수 없다.")
    void 엔티티_transient_column_동시사용_검증() {

        // given
        Class<InvalidDummyEntity> invalidEntity = InvalidDummyEntity.class;

        // when & then
        Assertions.assertThatThrownBy(() -> new TableEntity<>(invalidEntity))
                .isInstanceOf(InvalidEntityException.class)
                .hasMessageContaining("@Transient & @Column cannot be used in same field");
    }
}

// 이 클래스에는 @Entity가 없다.
class DummyClass {

}


@Entity
class InvalidDummyEntity {

    @Id
    private Long id;

    @Column
    @Transient
    private String thisIdNotField2; // 사용
}

