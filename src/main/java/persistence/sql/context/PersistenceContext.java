package persistence.sql.context;

import persistence.sql.dml.MetadataLoader;
import persistence.sql.entity.CollectionEntry;
import persistence.sql.entity.EntityEntry;
import persistence.sql.entity.data.Status;

import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.Collection;

public interface PersistenceContext {

    <T> EntityEntry addEntry(T entity, Status status, EntityPersister entityPersister);

    <T> EntityEntry addLoadingEntry(Object primaryKey, Class<T> returnType);

    <T, ID> EntityEntry getEntry(Class<T> entityType, ID id);

    CollectionEntry getCollectionEntry(CollectionKeyHolder key);

    CollectionEntry addCollectionEntry(CollectionKeyHolder key, CollectionEntry collectionEntry);

    <T, ID> void deleteEntry(T entity, ID id);

    void cleanup();

    void dirtyCheck(EntityPersister persister);
}
