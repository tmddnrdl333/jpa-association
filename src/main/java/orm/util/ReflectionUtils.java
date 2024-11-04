package orm.util;

import orm.exception.ReflectionException;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReflectionUtils {

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
}
