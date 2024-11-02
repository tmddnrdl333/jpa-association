package persistence.entity;

import jdbc.JdbcTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

public class EntityManagerImpl implements EntityManager {
    private final PersistenceContext persistenceContext;
    private final EntityLoader entityLoader;
    private final EntityPersister entityPersister;

    public EntityManagerImpl(JdbcTemplate jdbcTemplate,
                             PersistenceContext persistenceContext,
                             EntityPersister entityPersister) {

        this.persistenceContext = persistenceContext;
        this.entityLoader = new EntityLoader(jdbcTemplate);
        this.entityPersister = entityPersister;
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        final EntityKey entityKey = new EntityKey((Long) id, clazz);
        final EntityEntry entityEntry = getEntityEntryOrDefault(entityKey, () -> EntityEntry.loading((Serializable) id));

        if (entityEntry.isManaged()) {
            return clazz.cast(persistenceContext.getEntity(entityKey));
        }

        if (entityEntry.isNotReadable()) {
            throw new IllegalArgumentException("Entity is not managed: " + clazz.getSimpleName());
        }

        final T loaded = entityLoader.loadEntity(clazz, entityKey);
        addEntityInContext(entityKey, loaded);
        addManagedEntityEntry(entityKey, entityEntry);
        return loaded;
    }

    private EntityEntry getEntityEntryOrDefault(EntityKey entityKey, Supplier<EntityEntry> defaultEntrySupplier) {
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entityKey);
        if (entityEntry == null) {
            return defaultEntrySupplier.get();
        }

        return entityEntry;
    }

    @Override
    public void persist(Object entity) {
        if (entityPersister.hasId(entity)) {
            final EntityEntry entityEntry = persistenceContext.getEntityEntry(
                    new EntityKey(entityPersister.getEntityId(entity), entity.getClass())
            );

            if (entityEntry == null) {
                throw new IllegalArgumentException("No Entity Entry with id: " + entityPersister.getEntityId(entity));
            }

            if (entityEntry.isManaged()) {
                return;
            }

            throw new IllegalArgumentException("Entity already persisted");
        }

        saveEntity(entity, entityPersister);
    }

    private void saveEntity(Object entity, EntityPersister entityPersister) {
        entityPersister.insert(entity);

        final EntityEntry entityEntry = EntityEntry.inSaving();
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(entity), entity.getClass());

        addEntityInContext(entityKey, entity);
        addManagedEntityEntry(entityKey, entityEntry);
        manageChildEntity(entityPersister, entity);
    }

    private void manageChildEntity(EntityPersister entityPersister, Object entity) {
        final Collection<Object> childCollections = entityPersister.getChildCollections(entity);

        if (childCollections.isEmpty()) {
            return;
        }

        childCollections.forEach(childEntity -> {
            if (childEntity != null) {
                if (entityPersister.hasId(childEntity)) {
                    EntityKey entityKey = new EntityKey(entityPersister.getEntityId(childEntity), childEntity.getClass());
                    addEntityInContext(entityKey, childEntity);
                    addManagedEntityEntry(entityKey, EntityEntry.inSaving());
                }
            }
        });
    }

    @Override
    public void remove(Object entity) {
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(entity), entity.getClass());
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entityKey);
        checkManagedEntity(entity, entityEntry);

        entityEntry.updateStatus(Status.DELETED);
        entityPersister.delete(entity);
        persistenceContext.removeEntity(entityKey);
    }

    @Override
    public <T> T merge(T entity) {
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(entity), entity.getClass());
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entityKey);
        checkManagedEntity(entity, entityEntry);

        final EntitySnapshot entitySnapshot = persistenceContext.getDatabaseSnapshot(entityKey);
        if (entitySnapshot.hasDirtyColumns(entity)) {
            entityPersister.update(entity);
        }

        addEntityInContext(entityKey, entity);
        addManagedEntityEntry(entityKey, entityEntry);
        return entity;
    }

    @Override
    public void clear() {
        persistenceContext.clear();
    }

    private void checkManagedEntity(Object entity, EntityEntry entityEntry) {
        if (entityEntry == null) {
            throw new IllegalStateException("Can not find entity in persistence context: "
                    + entity.getClass().getSimpleName());
        }

        if (!entityEntry.isManaged()) {
            throw new IllegalArgumentException("Detached entity can not be merged: "
                    + entity.getClass().getSimpleName());
        }
    }

    private void addEntityInContext(EntityKey entityKey, Object entity) {
        persistenceContext.addEntity(entityKey, entity);
        persistenceContext.addDatabaseSnapshot(entityKey, entity);
    }

    private void addManagedEntityEntry(EntityKey entityKey, EntityEntry entityEntry) {
        entityEntry.updateStatus(Status.MANAGED);
        persistenceContext.addEntry(entityKey, entityEntry);
    }

}
