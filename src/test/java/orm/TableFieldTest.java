package orm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.ddl.Person;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TableFieldTest {

    @Test
    @DisplayName("테이블의 필드들은 @Transient을 제외하고 등록된다.")
    void transient_테스트() {

        // given
        TableEntity<DummyEntity> tableEntity = new TableEntity<>(DummyEntity.class);

        // when
        List<TableField> allFields = tableEntity.getAllFields();

        // then
        assertThat(allFields).hasSize(3);
    }

    @Test
    @DisplayName("데이터가 들어있는 엔티티로 TableEntit y를 만들면 하위 TableField 들이 값을 가지고 있다.")
    void 테이블_필드_데이터존재_검증() {

        // given
        Person person = new Person(1L, 30, "설동민");; // 모든 필드에 데이터가 있음
        var tableEntity = new TableEntity<>(person);

        // when
        List<TableField> allFields = tableEntity.getAllFields();

        // then
        assertThat(allFields).allSatisfy(e -> {
            assertThat(e.getFieldValue()).isNotNull();
        });
    }
}

@Entity
class DummyEntity {

    @Id
    private Long id;

    @Column
    private Integer fieldName;

    @Column(name = "annotated_name")
    private String fieldName2;

    @Transient
    private String thisIdNotField;

    public void setFieldName2(String fieldName2) {
        this.fieldName2 = fieldName2;
    }
}
