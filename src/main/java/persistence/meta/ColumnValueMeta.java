package persistence.meta;

import static persistence.validator.AnnotationValidator.isNotPresent;

import jakarta.persistence.Id;
import java.lang.reflect.Field;

public record ColumnValueMeta(Field field,
                              Object value) {

    public static ColumnValueMeta of(Field field, Object instance) {
        return new ColumnValueMeta(
                field,
                getValue(field, instance)
        );
    }

    private static Object getValue(Field field, Object instance) {
        try {
            field.setAccessible(true);
            return field.get(instance);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    public boolean isNotPrimaryKey() {
        return isNotPresent(field, Id.class);
    }

}
