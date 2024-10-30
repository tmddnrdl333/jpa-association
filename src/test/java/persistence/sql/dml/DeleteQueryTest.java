package persistence.sql.dml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixture.EntityWithId;

import static org.assertj.core.api.Assertions.*;

class DeleteQueryTest {
    @Test
    @DisplayName("delete 쿼리를 생성한다.")
    void delete() {
        // given
        final EntityWithId entity = new EntityWithId(1L, "Jaden", 30, "test@email.com");
        final DeleteQuery deleteQuery = new DeleteQuery();

        // when
        final String sql = deleteQuery.delete(entity);

        // then
        assertThat(sql).isEqualTo("DELETE FROM users WHERE id = 1");
    }
}
