package persistence.sql.component;

import persistence.sql.NameUtils;

public class TableInfo {
    private Class<?> tableType;
    private String tableName;

    public TableInfo(Class<?> tableType) {
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
