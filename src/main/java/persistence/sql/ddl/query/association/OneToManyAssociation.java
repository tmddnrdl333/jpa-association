package persistence.sql.ddl.query.association;

import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import persistence.exception.NotExistException;
import persistence.exception.UnknownException;
import persistence.sql.ddl.query.constraint.ForeignKeyConstraint;
import persistence.sql.ddl.query.constraint.SoftForeignKeyConstraint;

public class OneToManyAssociation implements Association {

    @Override
    public ForeignKeyConstraint foreignKeyConstraint(Class<?> clazz, Field field) {
        if (hasJoinColumnAnnotation(field)) {
            return new SoftForeignKeyConstraint(
                    childClass(field),
                    parentIdField(clazz),
                    joinColumnName(field));
        }
        return null;
    }

    private String joinColumnName(Field field) {
        if (hasNotJoinColumnAnnotation(field)) {
            throw new NotExistException("@JoinColumn annotation. field: " + field.getName());
        }
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        return joinColumn.name();
    }

    private boolean hasJoinColumnAnnotation(Field field) {
        return field.isAnnotationPresent(JoinColumn.class);
    }

    private boolean hasNotJoinColumnAnnotation(Field field) {
        return !hasJoinColumnAnnotation(field);
    }

    private Class<?> childClass(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type childType = parameterizedType.getActualTypeArguments()[0];

            if (childType instanceof Class<?>) {
                return (Class<?>) childType;
            }
        }
        throw new UnknownException("Type " + genericType);
    }

    private Field parentIdField(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new NotExistException("@Id."));
    }

}
