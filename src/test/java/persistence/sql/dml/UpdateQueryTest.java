package persistence.sql.dml;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.fixture.EntityWithId;
import persistence.meta.EntityTable;

import static org.assertj.core.api.Assertions.*;

class UpdateQueryTest {
    @Test
    @DisplayName("update 쿼리를 생성한다.")
    void update() {
        // given
        final UpdateQuery updateQuery = new UpdateQuery();
        final EntityWithId entity = new EntityWithId(1L, "Jackson", 20, "test@email.com");
        final EntityTable entityTable = new EntityTable(entity);

        // when
        final String sql = updateQuery.update(entity, entityTable.getEntityColumns());

        // then
        assertThat(sql).isEqualTo("UPDATE users SET id = 1, nick_name = 'Jackson', old = 20, email = 'test@email.com' WHERE id = 1");
    }
}
