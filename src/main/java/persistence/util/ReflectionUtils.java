package persistence.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

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

}
