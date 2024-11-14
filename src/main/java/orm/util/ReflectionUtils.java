package orm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.exception.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectionUtils {

    private static final Logger logger = LoggerFactory.getLogger(ReflectionUtils.class);

    /**
     * 리플랙션을 통해 필드를 복사해서 새로운 객체를 생성한다.
     *
     * @param source 원본 객체
     * @return Object의 Equality는 같지만, identity는 다른 객체
     */
    public static <T> T deepCopyObject(T source) {
        Class<?> sourceClass = source.getClass();

        try {
            Object newObject = sourceClass.getDeclaredConstructor().newInstance();
            Map<String, Field> originFieldMap = Arrays.stream(sourceClass.getDeclaredFields())
                    .collect(Collectors.toMap(Field::getName, Function.identity()));

            Field[] targetFields = newObject.getClass().getDeclaredFields();

            for (Field targetField : targetFields) {
                Field originField = originFieldMap.get(targetField.getName());
                targetField.setAccessible(true);
                originField.setAccessible(true);
                targetField.set(newObject, originField.get(source));
            }

            return (T) newObject;
        } catch (Exception e) {
            throw new ReflectionException("Failed to deep copy object", e);
        }
    }

    public static void setFieldValue(Field declaredField, Object entity, Object fieldValue) {
        declaredField.setAccessible(true);
        try {
            if (fieldValue != null) {
                declaredField.set(entity, fieldValue);
            }
        } catch (IllegalAccessException e) {
            logger.error("Cannot access field: " + declaredField.getName(), e);
        }
    }

    // 필드의 제네릭 타입에서 실제 타입을 추출한다.
    public static Class<?> extractGenericSignature(Field field) {
        Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType parameterizedType)) {
            return null;
        }

        Type actualType = parameterizedType.getActualTypeArguments()[0];

        try {
            return Class.forName(actualType.getTypeName());
        } catch (ClassNotFoundException e) {
            throw new ReflectionException("ClassNotFound" + e);
        }
    }

    public static Object getFieldValueFromObject(Object object, Field field) {
        field.setAccessible(true);
        try {
            return field.get(object);
        } catch (IllegalAccessException e) {
            throw new ReflectionException("오브젝트 필드의 데이터를 가져 올 수 없습니다." + e);
        }
    }
}
