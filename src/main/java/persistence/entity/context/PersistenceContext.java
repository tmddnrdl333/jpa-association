package persistence.entity.context;

import java.util.Optional;
import persistence.entity.context.snapshot.EntitySnapshot;
import persistence.entity.context.status.EntityStatus;

public interface PersistenceContext {

    <T, ID> Optional<T> getEntity(ID id, Class<T> entityType);

    void addEntity(Object entity);

    void removeEntity(Object entity);

    void addDatabaseSnapshot(Object entity);

    <T> EntitySnapshot getDatabaseSnapshot(T entity);

    void removeDatabaseSnapshot(Object entity);

    void addEntityEntry(Object entity, EntityStatus status);

    void updateEntityEntry(Object entity, EntityStatus status);

    <T> boolean isDirty(T entity);

}
