package persistence.sql.component;

import persistence.sql.NameUtils;

import java.lang.reflect.Field;

public class ColumnInfo {
    private TableInfo tableInfo;
    private String columnName;

    private ColumnInfo(TableInfo tableInfo, Field field) {
        this.tableInfo = tableInfo;
        this.columnName = NameUtils.getColumnName(field);
    }

    public static ColumnInfo of(TableInfo tableInfo, Field columnField) {
        return new ColumnInfo(tableInfo, columnField);
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
