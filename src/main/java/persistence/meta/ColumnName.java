package persistence.meta;

import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;

import java.lang.reflect.Field;
import java.util.Objects;

public class ColumnName {
    private final String name;

    public ColumnName(Field field) {
        this.name = getName(field);
    }

    public String value() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnName that = (ColumnName) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    private String getName(Field field) {
        final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (Objects.nonNull(joinColumn) && Objects.nonNull(joinColumn.name()) && !joinColumn.name().isBlank()) {
            return joinColumn.name();
        }

        final Column column = field.getAnnotation(Column.class);
        if (Objects.nonNull(column) && Objects.nonNull(column.name()) && !column.name().isBlank()) {
            return column.name();
        }
        return field.getName();
    }
}
