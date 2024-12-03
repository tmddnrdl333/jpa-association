package persistence.sql.component;

import jakarta.persistence.Id;
import persistence.sql.NameUtils;

import java.lang.reflect.Field;
import java.util.Arrays;

public class TableInfo {
    private Class<?> entityClass;
    private String tableName;

    private TableInfo(Class<?> entityClass) {
        this.entityClass = entityClass;
        this.tableName = NameUtils.getTableName(entityClass);
    }

    public static TableInfo from(Class<?> entityClass) {
        return new TableInfo(entityClass);
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnInfo getIdColumn() {
        Field idColumnField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findAny()
                .orElseThrow();
        return ColumnInfo.of(this, idColumnField);
    }

    public ColumnInfo getColumn(String columnName) {
        Field columnField = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> columnName.equals(NameUtils.getColumnName(field)))
                .findAny()
                .orElseThrow();
        return ColumnInfo.of(this, columnField);
    }
}
