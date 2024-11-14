package orm;

import java.util.HashMap;
import java.util.Map;

public record TableAlias(
        String tableName,
        String alias
) {

    private static final ThreadLocal<Map<String, Integer>> threadLocalTableNameCounter = ThreadLocal.withInitial(HashMap::new);

    public TableAlias(String tableName) {
        this(tableName, "%s_%s".formatted(tableName, generateTableAliasIndex(tableName)));
    }

    /**
     * 스레드 기준으로 테이블 별 alias index 생성
     */
    private static Integer generateTableAliasIndex(String tableName) {
        Map<String, Integer> counterMap = threadLocalTableNameCounter.get();
        counterMap.put(tableName, counterMap.getOrDefault(tableName, 0) + 1);
        return counterMap.get(tableName);
    }
}
