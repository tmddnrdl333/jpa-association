package orm.validator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;
import orm.exception.InvalidEntityException;

import java.lang.reflect.Field;

public class EntityValidator<E> {

    private final Class<?> entityClass;

    public EntityValidator(E entity) {
        this.entityClass = entity.getClass();
    }

    public void validate() {
        throwIfNotEntity();
        throwIfTransientColumnFound();
    }

    private void throwIfNotEntity() {
        if (entityClass.getAnnotation(Entity.class) == null) {
            throw new InvalidEntityException(entityClass.getName() + " is not an entity");
        }
    }

    private void throwIfTransientColumnFound() {
        for (Field declaredField : entityClass.getDeclaredFields()) {
            boolean transientAnnotated = declaredField.isAnnotationPresent(Transient.class);
            boolean columnAnnotated = declaredField.isAnnotationPresent(Column.class);

            if (transientAnnotated && columnAnnotated) {
                throw new InvalidEntityException(String.format(
                        "class %s @Transient & @Column cannot be used in same field"
                        , entityClass.getName())
                );
            }
        }
    }
}
