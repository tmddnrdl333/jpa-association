package persistence.sql.dml.impl;

import jakarta.persistence.CascadeType;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.OneToMany;
import persistence.sql.EntityLoaderFactory;
import persistence.sql.clause.Clause;
import persistence.sql.context.EntityPersister;
import persistence.sql.context.PersistenceContext;
import persistence.sql.dml.EntityManager;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.entity.EntityEntry;
import persistence.sql.entity.data.Status;
import persistence.sql.loader.EntityLoader;
import persistence.sql.transaction.Transaction;
import persistence.sql.transaction.impl.EntityTransaction;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class DefaultEntityManager implements EntityManager {
    private final PersistenceContext persistenceContext;
    private final EntityPersister entityPersister;
    private final EntityLoaderFactory entityLoaderFactory;
    private final Transaction transaction;


    public DefaultEntityManager(PersistenceContext persistenceContext, EntityPersister entityPersister) {
        this.persistenceContext = persistenceContext;
        this.entityPersister = entityPersister;
        this.entityLoaderFactory = EntityLoaderFactory.getInstance();
        this.transaction = new EntityTransaction(this);
    }

    @Override
    public Transaction getTransaction() {
        Connection connection = entityPersister.getConnection();
        transaction.connect(connection);
        return transaction;
    }

    @Override
    public <T> void persist(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }

        if (!isNew(entity)) {
            throw new EntityExistsException("Entity already exists");
        }
        entityPersister.insert(entity);
        EntityEntry entityEntry = persistenceContext.addEntry(entity, Status.SAVING, entityPersister);

        if (existsPersistChildEntity(entity)) {
            persistChildEntity(entity);
        }

        if (!transaction.isActive()) {
            entityEntry.updateStatus(Status.MANAGED);
        }
    }

    private <T> void persistChildEntity(T entity) {
        MetadataLoader<?> loader = entityLoaderFactory.getLoader(entity.getClass()).getMetadataLoader();
        List<Field> fields = loader.getFieldAllByPredicate(this::isCascadePersist);

        for (Field field : fields) {
            persistChildEntities(entity, field);
        }
    }

    private <T> void persistChildEntities(T entity, Field field) {
        try {
            field.setAccessible(true);
            Collection<?> childEntities = (Collection<?>) field.get(entity);

            if (childEntities == null || childEntities.isEmpty()) {
                return;
            }

            childEntities.forEach(childEntity -> persistIfIsNewChildEntity(entity, childEntity));
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private <T> void persistIfIsNewChildEntity(T entity, Object childEntity) {
        if (isNew(childEntity)) {
            entityPersister.insert(childEntity, entity);
            EntityEntry entityEntry = persistenceContext.addEntry(childEntity, Status.SAVING, entityPersister);

            if (!transaction.isActive()) {
                entityEntry.updateStatus(Status.MANAGED);
            }
        }
    }

    private <T> boolean existsPersistChildEntity(T entity) {
        EntityLoader<?> entityLoader = entityLoaderFactory.getLoader(entity.getClass());
        MetadataLoader<?> loader = entityLoader.getMetadataLoader();
        List<Field> fields = loader.getFieldAllByPredicate(this::isCascadePersist);

        return fields.stream().anyMatch(field -> isNotEmptyField(entity, field));
    }

    private boolean isCascadePersist(Field field) {
        OneToMany anno = field.getAnnotation(OneToMany.class);

        return anno != null
                && Arrays.stream(anno.cascade())
                .anyMatch(cascadeType -> CascadeType.PERSIST == cascadeType);
    }

    private <T> boolean isNotEmptyField(T entity, Field field) {
        try {
            field.setAccessible(true);
            Object value = field.get(entity);

            return Collection.class.isAssignableFrom(field.getType()) && !((Collection<?>) value).isEmpty();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private boolean isNew(Object entity) {
        EntityLoader<?> entityLoader = entityLoaderFactory.getLoader(entity.getClass());
        MetadataLoader<?> loader = entityLoader.getMetadataLoader();

        Field primaryKeyField = loader.getPrimaryKeyField();
        Object idValue = Clause.extractValue(primaryKeyField, entity);
        if (idValue == null) {
            return true;
        }

        return find(loader.getEntityType(), idValue) == null;
    }

    @Override
    public <T> T merge(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        EntityLoader<?> entityLoader = entityLoaderFactory.getLoader(entity.getClass());
        MetadataLoader<?> loader = entityLoader.getMetadataLoader();

        if (isNew(entity)) {
            persist(entity);
            return entity;
        }

        Object id = Clause.extractValue(loader.getPrimaryKeyField(), entity);

        EntityEntry entry = persistenceContext.getEntry(entity.getClass(), id);
        if (entry == null) {
            throw new IllegalStateException("Entity not found. ");
        }

        entry.updateEntity(entity);
        if (!transaction.isActive()) {
            entityPersister.update(entity, entry.getSnapshot());
            entry.synchronizingSnapshot();
        }

        return entity;
    }

    @Override
    public <T> void remove(T entity) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        EntityLoader<?> entityLoader = entityLoaderFactory.getLoader(entity.getClass());
        MetadataLoader<?> loader = entityLoader.getMetadataLoader();

        Object id = Clause.extractValue(loader.getPrimaryKeyField(), entity);

        EntityEntry entityEntry = persistenceContext.getEntry(entity.getClass(), id);
        if (entityEntry == null) {
            throw new IllegalStateException("Entity not found. ");
        }

        entityEntry.updateStatus(Status.DELETED);
        if (!transaction.isActive()) {
            entityPersister.delete(entity);
            persistenceContext.deleteEntry(entity, id);
        }
    }

    @Override
    public <T> T find(Class<T> returnType, Object primaryKey) {
        if (primaryKey == null) {
            throw new IllegalArgumentException("Primary key must not be null");
        }

        EntityEntry entry = persistenceContext.getEntry(returnType, primaryKey);

        if (entry != null) {
            return returnType.cast(entry.getEntity());
        }

        entry = persistenceContext.addLoadingEntry(primaryKey, returnType);
        EntityLoader<T> entityLoader = entityLoaderFactory.getLoader(returnType);

        T loadedEntity = entityLoader.load(primaryKey);
        if (loadedEntity != null) {
            entry.updateEntity(loadedEntity);
            entry.updateStatus(Status.MANAGED);
        }

        if (entityLoader.existLazyLoading()) {
            entityLoader.updateLazyLoadingField(loadedEntity, persistenceContext, (collectionKeyHolder, collectionEntry) -> {
                if (transaction.isActive()) {
                    persistenceContext.addCollectionEntry(collectionKeyHolder, collectionEntry);
                }
            });
        }

        return loadedEntity;
    }

    @Override
    public <T> List<T> findAll(Class<T> entityClass) {
        EntityLoader<T> entityLoader = entityLoaderFactory.getLoader(entityClass);

        List<T> loadedEntities = entityLoader.loadAll();
        for (T loadedEntity : loadedEntities) {
            persistenceContext.addEntry(loadedEntity, Status.MANAGED, entityPersister);
        }

        return loadedEntities;
    }

    @Override
    public void onFlush() {
        persistenceContext.dirtyCheck(entityPersister);
        persistenceContext.cleanup();
    }
}
