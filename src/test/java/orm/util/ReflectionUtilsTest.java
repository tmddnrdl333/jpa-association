package orm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Person;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionUtilsTest {

    @Test
    @DisplayName("""
            ReflectionUtils 인스턴스 deep copy 테스트
            - deep copy를 통해 새로운 객체를 생성하면 Object의 Identity가 서로 다르고, Equality는 같아야한다.
            """
    )
    void reflectionUtils_deep_copy_test() {
        // given
        Person origin = new Person(null, 30, "설동민");

        // when
        Person target = ReflectionUtils.deepCopyObject(origin);

        // then
        assertThat(target)
                .isNotSameAs(origin) // Object의 Identity는 서로 다름
                .satisfies(e -> {
                    assertThat(e.getId()).isEqualTo(origin.getId()); // Object의 Equality는 같다.
                    assertThat(e.getAge()).isEqualTo(origin.getAge());
                    assertThat(e.getName()).isEqualTo(origin.getName());
                });
    }
}
