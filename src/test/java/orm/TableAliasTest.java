package orm;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;

class TableAliasTest {

    @Test
    @DisplayName("테이블 별칭은 스레드별로 카운터되며, 테이블 별칭을 생성할 때마다 카운터가 증가한다.")
    void tableAliasTest() {
        격리된_스레드에서_실행(() -> {
            // given
            final String tableName = "person";

            // when
            TableAlias 별칭_1 = new TableAlias(tableName);
            TableAlias 별칭_2 = new TableAlias(tableName);

            // then
            assertThat(별칭_1.alias()).isEqualTo("person_1");
            assertThat(별칭_2.alias()).isEqualTo("person_2");
        });
    }

    private void 격리된_스레드에서_실행(Runnable runnable) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(runnable);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
