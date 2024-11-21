package persistence.entity.context;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import persistence.entity.context.status.EntityEntry;
import persistence.entity.context.snapshot.EntitySnapshot;
import persistence.entity.context.status.EntityStatus;
import persistence.exception.NotExistException;

public class DefaultPersistenceContext implements PersistenceContext {

    private final Map<EntityKey, Object> context = new HashMap<>();
    private final Map<EntityKey, EntitySnapshot> snapshots = new HashMap<>();
    private final Map<EntityKey, EntityEntry> entries = new HashMap<>();

    @Override
    public <T, ID> Optional<T> getEntity(ID id, Class<T> entityType) {
        EntityKey key = new EntityKey(id, entityType);
        return Optional.ofNullable(entityType.cast(context.get(key)));
    }

    @Override
    public void addEntity(Object entity) {
        EntityKey key = new EntityKey(entity);
        if (isContainsKey(context, key)) {
            context.remove(key);
            context.put(key, entity);
            return;
        }
        context.put(key, entity);
    }

    @Override
    public void removeEntity(Object entity) {
        EntityKey key = new EntityKey(entity);
        if (isNotContainsKey(context, key)) {
            throw new NotExistException(MessageFormat.format(
                    "Entity in context. entity id: {0}, entity type: {1}", key.key(),
                    entity.getClass().getSimpleName()));
        }
        context.remove(key);
    }

    @Override
    public void addDatabaseSnapshot(Object entity) {
        EntityKey key = new EntityKey(entity);
        snapshots.put(key, new EntitySnapshot(entity));
    }

    @Override
    public <T> EntitySnapshot getDatabaseSnapshot(T entity) {
        EntityKey key = new EntityKey(entity);
        return snapshots.get(key);
    }

    @Override
    public void removeDatabaseSnapshot(Object entity) {
        EntityKey key = new EntityKey(entity);
        if (isContainsKey(snapshots, key)) {
            snapshots.remove(key);
        }
    }

    @Override
    public void addEntityEntry(Object entity, EntityStatus status) {
        EntityKey key = new EntityKey(entity);

        if (entries.containsKey(key)) {
            EntityEntry entry = entries.get(key);
            entry.updateStatus(status);
            return;
        }

        EntityEntry entry = new EntityEntry(key, status);
        entries.put(key, entry);
    }

    @Override
    public void updateEntityEntry(Object entity, EntityStatus status) {
        EntityKey key = new EntityKey(entity);
        EntityEntry entry = entries.get(key);
        if (entry == null) {
            throw new NotExistException(MessageFormat.format("EntityEntry id: {0}, type: {1}", key.key(), entity.getClass().getSimpleName()));
        }
        entry.updateStatus(status);
    }

    @Override
    public <T> boolean isDirty(T entity) {
        EntitySnapshot snapshot = getDatabaseSnapshot(entity);
        return snapshot.hasDifferenceWith(entity);
    }

    private <V> boolean isContainsKey(Map<EntityKey, V> map, EntityKey key) {
        return map.containsKey(key);
    }

    private <V> boolean isNotContainsKey(Map<EntityKey, V> map, EntityKey key) {
        return !isContainsKey(map, key);
    }

}