package orm.dsl.holder;

import jakarta.persistence.Id;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.exception.CannotExtractEntityFieldValueException;
import orm.exception.InvalidIdMappingException;
import orm.util.CollectionUtils;

import java.lang.reflect.Field;
import java.util.Arrays;

public class EntityIdHolder<E> {
    private static final Logger log = LoggerFactory.getLogger(EntityIdHolder.class);

    private final Field idField;
    private final Object idValue;
    private final Class<E> entityClass;

    public EntityIdHolder(E entity) {
        this.entityClass = (Class<E>) entity.getClass();
        this.idField = extractIdField(entity.getClass());
        this.idValue = extractIdValue(entity);
    }

    public Field getIdField() {
        return idField;
    }

    public Object getIdValue() {
        return idValue;
    }

    public Class<E> getEntityClass() {
        return entityClass;
    }

    private Field extractIdField(Class<?> entityClass) {
        Field[] declaredFields = entityClass.getDeclaredFields();

        var idList = Arrays.stream(declaredFields)
                .filter(field -> field.isAnnotationPresent(Id.class))
                .toList();

        if (CollectionUtils.isEmpty(idList) || idList.size() != 1) {
            throw new InvalidIdMappingException("Entity must have one @Id field");
        }

        return idList.getFirst();
    }

    public Object extractIdValue(E entity) {
        return extractFieldValue(idField, entity);
    }

    private <T> Object extractFieldValue(Field field, T entity) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.error("Cannot access field: {}", field.getName(), e);
            throw new CannotExtractEntityFieldValueException("Cannot extract field value: " + field.getName(), e);
        }
    }
}
