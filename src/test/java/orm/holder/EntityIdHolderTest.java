package orm.holder;

import jakarta.persistence.Entity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.dsl.holder.EntityIdHolder;
import orm.exception.InvalidIdMappingException;
import persistence.sql.ddl.Person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EntityIdHolderTest {

    @Test
    @DisplayName("EntityIdExtractor는 엔티티의 ID 필드와 값을 추출한다.")
    void EntityIdExtractor_테스트() {

        // given
        Person newPerson = new Person(1L, 30, "설동민");

        // when
        var idExtractor = new EntityIdHolder<>(newPerson);

        // then
        assertThat(idExtractor)
                .satisfies(holder -> {
                    assertThat(holder.getIdField().getName()).isEqualTo("id");
                    assertThat(holder.getIdValue()).isEqualTo(1L);
                    assertThat(holder.getEntityClass()).isEqualTo(Person.class);
                });
    }

    @Test
    @DisplayName("EntityIdExtractor는 엔티티의 ID 값이 null 인경우 null을 리턴한다.")
    void EntityIdExtractor_테스트_id가_null일때() {

        // given
        Person newPerson = new Person(null, 30, "설동민");

        // when
        var idExtractor = new EntityIdHolder<>(newPerson);

        // then
        assertThat(idExtractor.getIdValue()).isNull();
    }

    @Test
    @DisplayName("EntityIdExtractor는 Id 애너테이션이 반드시 있어야한다.")
    void EntityIdExtractor는_id값_필수() {

        // given
        DummyClass newPerson = new DummyClass();

        // when & then
        assertThatThrownBy(() -> new EntityIdHolder<>(newPerson))
                .isInstanceOf(InvalidIdMappingException.class)
                .hasMessageContaining("Entity must have one @Id field");
    }
}

@Entity
class DummyClass {

}
