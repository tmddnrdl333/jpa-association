package persistence.sql.component;

import persistence.sql.NameUtils;

import java.lang.reflect.Field;

public class ColumnInfo {
    private TableInfo tableInfo;
    private String columnName;

    public ColumnInfo(TableInfo tableInfo, Field field) {
        this.tableInfo = tableInfo;
        this.columnName = NameUtils.getColumnName(field);
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public String getColumnName() {
        return columnName;
    }

    public String getFullName() {
        return tableInfo.getTableName() + "." + columnName;
    }
}
