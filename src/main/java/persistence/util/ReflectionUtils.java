package persistence.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;

public class ReflectionUtils {

    private ReflectionUtils() {
    }

    public static Class<?> collectionClass(Type type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();

            if (typeArguments != null && typeArguments.length > 0) {
                Type typeArgument = typeArguments[0];
                try {
                    return Class.forName(typeArgument.getTypeName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new IllegalArgumentException();
    }

    @SuppressWarnings("unchecked")
    public static Class<? extends Collection<Object>> getCollectionFieldType(Field field) {
        if (Collection.class.isAssignableFrom(field.getType())) {
            return (Class<? extends Collection<Object>>) field.getType();
        }
        throw new IllegalArgumentException("Field is not a Collection type");
    }
}
