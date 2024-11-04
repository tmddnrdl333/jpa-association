package orm;

import orm.dsl.holder.EntityIdHolder;
import orm.life_cycle.EntityEntry;
import orm.life_cycle.Status;

public interface PersistenceContext {

    <T> T getEntity(Class<T> entityClazz, Object id);

    <T> T addEntity(T entity);

    <T> boolean contains(EntityIdHolder<T> idHolder);

    void removeEntity(Object entity);

    <T> Object getDatabaseSnapshot(EntityIdHolder<T> idHolder, EntityPersister entityPersister);

    <T> EntityEntry getEntry(EntityIdHolder<T> idHolder);

    EntityEntry getEntry(Object entity);

    EntityEntry addEntry(Object entity, Status status);

    EntityEntry addEntry(EntityKey entityKey, Status status);

    void removeEntry(EntityKey entityKey);
}
