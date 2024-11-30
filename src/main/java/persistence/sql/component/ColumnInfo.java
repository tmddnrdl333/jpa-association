package persistence.sql.component;

import persistence.sql.NameUtils;

import java.lang.reflect.Field;

public class ColumnInfo {
    private TableInfo tableInfo;
    private Class<?> columnType;
    private String columnName;

    public ColumnInfo(TableInfo tableInfo, Field field) {
        this.tableInfo = tableInfo;
        this.columnType = field.getType();
        this.columnName = NameUtils.getColumnName(field);
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public String getColumnName() {
        return columnName;
    }
}
