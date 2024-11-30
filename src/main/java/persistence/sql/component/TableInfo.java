package persistence.sql.component;

import persistence.sql.NameUtils;

public class TableInfo {
    private Class<?> tableType;
    private String tableName;

    public static TableInfo from(Class<?> tableType) {
        return new TableInfo(tableType);
    }

    private TableInfo(Class<?> tableType) {
        this.tableType = tableType;
        this.tableName = NameUtils.getTableName(tableType);
    }

    public Class<?> getTableType() {
        return tableType;
    }

    public String getTableName() {
        return tableName;
    }
}
