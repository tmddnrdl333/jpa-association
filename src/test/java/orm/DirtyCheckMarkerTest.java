package orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import orm.dirty_check.DirtyCheckMarker;
import persistence.sql.ddl.Person;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

class DirtyCheckMarkerTest {

    @Test
    @DisplayName("""
            DirtyCheckMarker 는 비교할 원본객체와 스냅샷 객체를 받아 원본 객체의 변경된 필드를 체크한다.
            필드가 변경되면 원본 객체 비트셋의 값이 변경된다.
            """
    )
    void DirtyCheckMarker_테스트() {
        // given
        var snapshot = new TableEntity<>(new Person(1L, 30, "설동민"));
        var person = new TableEntity<>(new Person(1L, 20, "설동민 (아직 젊음)"));

        // when
        boolean hasDirty = new DirtyCheckMarker<>(person, snapshot).compareAndMarkChangedField();// 더티체킹 진행

        // then
        assertSoftly(softly -> {
            softly.assertThat(hasDirty).isTrue();
            softly.assertThat(person.getChangeFields()).asList()
                    .extracting("fieldValue")
                    .containsExactlyInAnyOrder(20, "설동민 (아직 젊음)");
        });
    }

    @Test
    @DisplayName("""
            DirtyCheckMarker 는 비교할 원본객체와 스냅샷 객체를 받아 원본 객체의 변경된 필드를 체크한다.
            필드가 변경되지 않으면, 원본 객체 비트셋의 값 또한 변경되지 않는다.
            """
    )
    void DirtyCheckMarker_false_테스트() {
        // given
        var snapshot = new TableEntity<>(new Person(1L, 30, "설동민"));
        var person = new TableEntity<>(new Person(1L, 30, "설동민"));

        // when
        boolean hasDirty = new DirtyCheckMarker<>(person, snapshot).compareAndMarkChangedField();// 더티체킹 진행

        // then
        assertSoftly(softly -> {
            softly.assertThat(hasDirty).isFalse();
            softly.assertThat(person.getChangeFields()).isEmpty();
        });
    }

}
