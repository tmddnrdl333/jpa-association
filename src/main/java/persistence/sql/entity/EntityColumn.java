package persistence.sql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.lang.reflect.Field;

public class EntityColumn {
    private final Field field;
    private final String columnName;
    private final boolean isPrimaryKey;
    private final boolean isNullable;
    private final boolean isGeneratedValue;
    private final boolean isTransient;

    private final OneToManyColumn oneToManyColumn;

    public EntityColumn(Field field, String columnName, boolean isPrimaryKey, boolean isNullable, boolean isGeneratedValue, boolean isTransient, OneToManyColumn oneToManyColumn) {
        this.field = field;
        this.columnName = columnName;
        this.isPrimaryKey = isPrimaryKey;
        this.isNullable = isNullable;
        this.isGeneratedValue = isGeneratedValue;
        this.isTransient = isTransient;
        this.oneToManyColumn = oneToManyColumn;
    }

    public static EntityColumn from(Field field) {
        OneToManyColumn oneToManyColumn = field.isAnnotationPresent(OneToMany.class) ? new OneToManyColumn(field) : null;
        return new EntityColumn(
                field,
                getFieldName(field),
                field.isAnnotationPresent(Id.class),
                isColumnNullable(field),
                field.isAnnotationPresent(GeneratedValue.class),
                field.isAnnotationPresent(Transient.class),
                oneToManyColumn
        );
    }

    private static boolean isColumnNullable(Field field) {
        if (!field.isAnnotationPresent(Column.class)) {
            return false;
        }
        Column annotation = field.getAnnotation(Column.class);
        return !annotation.nullable();
    }

    public Field getField() {
        return field;
    }

    public String getColumnName() {
        return columnName;
    }

    public static String getFieldName(Field field) {
        Column annotation = field.getAnnotation(Column.class);
        if (annotation == null) {
            return field.getName();
        }
        if (!annotation.name().isEmpty()) {
            return annotation.name();
        }

        return field.getName();
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public boolean isNullable() {
        return isNullable;
    }

    public boolean isGeneratedValue() {
        return isGeneratedValue;
    }

    public boolean isTransient() {
        return isTransient;
    }

    public boolean isOneToMany() {
        return field.isAnnotationPresent(OneToMany.class);
    }

    public OneToManyColumn getOneToManyColumn() {
        return oneToManyColumn;
    }

    public String getFieldValue(Object entity) {
        field.setAccessible(true);
        try {
            Object fieldValue = field.get(entity);
            if (fieldValue == null) {
                return "null";
            }
            return getFormattedId(fieldValue);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("필드에 접근할 수 없음", e);
        }
    }

    private String getFormattedId(Object idValue) {
        if (idValue instanceof String) {
            return String.format(("'%s'"), idValue);
        }
        return idValue.toString();
    }

}
