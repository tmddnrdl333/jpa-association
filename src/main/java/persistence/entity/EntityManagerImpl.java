package persistence.entity;

import jdbc.JdbcTemplate;

import java.io.Serializable;
import java.util.Collection;
import java.util.function.Supplier;

public class EntityManagerImpl implements EntityManager {
    private final PersistenceContext persistenceContext;
    private final JdbcTemplate jdbcTemplate;
    private final EntityLoader entityLoader;

    public EntityManagerImpl(JdbcTemplate jdbcTemplate,
                             PersistenceContext persistenceContext) {

        this.persistenceContext = persistenceContext;
        this.jdbcTemplate = jdbcTemplate;
        this.entityLoader = new EntityLoader(jdbcTemplate);
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
        final EntityPersister entityPersister = new EntityPersister(entity, jdbcTemplate);
        if (entityPersister.hasId()) {
            final EntityEntry entityEntry = persistenceContext.getEntityEntry(
                    new EntityKey(entityPersister.getEntityId(), entity.getClass())
            );

            if (entityEntry == null) {
                throw new IllegalArgumentException("No Entity Entry with id: " + entityPersister.getEntityId());
            }

            if (entityEntry.isManaged()) {
                return;
            }

            throw new IllegalArgumentException("Entity already persisted");
        }

        saveEntity(entity, entityPersister);
    }

    private void saveEntity(Object entity, EntityPersister entityPersister) {
        final EntityEntry entityEntry = EntityEntry.inSaving();

        entityPersister.insert(entity);
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(), entity.getClass());
        addEntityInContext(entityKey, entity);
        addManagedEntityEntry(entityKey, entityEntry);

        saveChildEntity(entityPersister, entity);
    }

    private void saveChildEntity(EntityPersister entityPersister, Object entity) {
        final Collection<Object> childCollections = entityPersister.getChildCollections(entity);

        if (childCollections.isEmpty()) {
            return;
        }

        childCollections.forEach(childEntity -> {
            if (childEntity != null) {
                final EntityPersister childEntityPersister = new EntityPersister(childEntity, jdbcTemplate);
                if (!childEntityPersister.hasId()) {
                    saveEntity(childEntity, childEntityPersister);
                }
            }
        });
    }

    @Override
    public void remove(Object entity) {
        final EntityPersister entityPersister = new EntityPersister(entity, jdbcTemplate);
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(), entity.getClass());
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entityKey);
        checkManagedEntity(entity, entityEntry);

        entityEntry.updateStatus(Status.DELETED);
        entityPersister.delete(entity);
        persistenceContext.removeEntity(entityKey);
    }

    @Override
    public <T> T merge(T entity) {
        final EntityPersister entityPersister = new EntityPersister(entity, jdbcTemplate);
        final EntityKey entityKey = new EntityKey(entityPersister.getEntityId(), entity.getClass());
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
