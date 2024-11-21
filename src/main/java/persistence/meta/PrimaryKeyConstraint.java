package persistence.meta;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;
import persistence.exception.NotExistException;

public record PrimaryKeyConstraint(ColumnMeta column,
                                   GenerationType generationType) {

    public static PrimaryKeyConstraint from(Class<?> clazz) {
        Field idField = idField(clazz);
        return new PrimaryKeyConstraint(new ColumnMeta(idField), generationType(idField));
    }

    private static Field idField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new NotExistException("identification."));
    }

    private static GenerationType generationType(Field field) {
        if (field.isAnnotationPresent(GeneratedValue.class)) {
            GeneratedValue annotation = field.getAnnotation(GeneratedValue.class);
            return annotation.strategy();
        }
        return GenerationType.IDENTITY;
    }

}
