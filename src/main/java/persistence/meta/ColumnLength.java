package persistence.meta;

import jakarta.persistence.Column;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class ColumnLength {
    private static final int MIN_LENGTH = 0;
    private static final int MAX_LENGTH = 255;

    private final List<Class<?>> lengthNeededTypes = List.of(String.class);
    private final int length;

    public ColumnLength(Field field) {
        this.length = getColumnLength(field);
    }

    public int value() {
        return length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnLength that = (ColumnLength) o;
        return length == that.length;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(length);
    }

    private int getColumnLength(Field field) {
        final Column column = field.getAnnotation(Column.class);
        if (!lengthNeededTypes.contains(field.getType())) {
            return MIN_LENGTH;
        }
        if (Objects.isNull(column)) {
            return MAX_LENGTH;
        }
        return column.length();
    }
}
