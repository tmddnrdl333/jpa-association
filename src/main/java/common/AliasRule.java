package common;

public class AliasRule {
    private AliasRule() {
    }

    public static String with(String tableName, String columnName) {
        return tableName + "_" + columnName;
    }
}
