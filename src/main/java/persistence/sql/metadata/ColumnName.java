package persistence.sql.metadata;

import static persistence.validator.AnnotationValidator.isNotBlank;
import static persistence.validator.AnnotationValidator.isPresent;

import jakarta.persistence.Column;
import java.lang.reflect.Field;

public record ColumnName(String value, TableAlias alias) {

    public ColumnName(Field field, TableName tableName) {
        this(getName(field), new TableAlias(tableName));
    }

    public ColumnName(Field field, Class<?> clazz) {
        this(getName(field), new TableAlias(clazz));
    }

    private static String getName(Field field) {
        if (isColumnNamePresent(field)) {
            return getColumnName(field);
        }
        return getFieldName(field);
    }

    private static boolean isColumnNamePresent(Field field) {
        return isPresent(field, Column.class) && isNotBlank(getColumnName(field));
    }

    private static String getColumnName(Field field) {
        Column column = field.getAnnotation(Column.class);
        return column.name();
    }

    private static String getFieldName(Field field) {
        return field.getName();
    }

}
