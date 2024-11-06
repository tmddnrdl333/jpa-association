package persistence.proxy.impl;

import org.jetbrains.annotations.NotNull;
import persistence.sql.EntityLoaderFactory;
import persistence.sql.context.CollectionKeyHolder;
import persistence.sql.context.KeyHolder;
import persistence.sql.context.PersistenceContext;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.entity.CollectionEntry;
import persistence.sql.loader.EntityLoader;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

public class LazyLoadingHandler<T> extends AbstractCollection<T> implements InvocationHandler, Serializable {
    private static final Logger logger = Logger.getLogger(LazyLoadingHandler.class.getName());
    private static final List<String> LOAD_REQUIRE_METHODS = List.of("get", "iterator", "forEach", "stream", "toArray");

    private final PersistenceContext persistenceContext;
    private final KeyHolder parentKeyHolder;
    private final EntityLoader<T> entityLoader;
    private List<T> target;
    private volatile boolean loaded = false;

    public LazyLoadingHandler(KeyHolder parentKeyHolder, EntityLoader<T> entityLoader, PersistenceContext persistenceContext) {
        this.persistenceContext = persistenceContext;
        this.parentKeyHolder = parentKeyHolder;
        this.entityLoader = entityLoader;
        this.target = Collections.emptyList();
    }

    public static <T> LazyLoadingHandler<T> newInstance(Object foreignKey,
                                                        Class<?> foreignType,
                                                        Class<T> targetClass,
                                                        PersistenceContext persistenceContext) {
        EntityLoader<T> entityLoader = EntityLoaderFactory.getInstance().getLoader(targetClass);

        return new LazyLoadingHandler<>(new KeyHolder(foreignType, foreignKey), entityLoader, persistenceContext);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (LOAD_REQUIRE_METHODS.contains(method.getName()) && !loaded) {
            logger.info("lazy loading... | from method: " + method.getName());
            realize();
        }

        return method.invoke(target, args);
    }

    private void realize() {
        CollectionEntry entry = persistenceContext.getCollectionEntry(getCollectionKeyHolder());
        if (entry == null) {
            throw new IllegalStateException("failed to lazily initialize a collection");
        }

        if (entry.isInitialize()) {
            target = entry.getEntries();
            loaded = true;
            return;
        }

        EntityLoader<?> targetLoader = EntityLoaderFactory.getInstance().getLoader(parentKeyHolder.entityType());
        target = entityLoader.loadAllByForeignKey(parentKeyHolder.key(), targetLoader.getMetadataLoader());
        entry.updateEntries((List<Object>) target);
        loaded = true;
        logger.info("completed lazy loading");
    }

    private CollectionKeyHolder getCollectionKeyHolder() {
        MetadataLoader<T> metadataLoader = entityLoader.getMetadataLoader();
        return new CollectionKeyHolder(parentKeyHolder.entityType(), parentKeyHolder.key(), metadataLoader.getEntityType());
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return target.iterator();
    }

    @Override
    public int size() {
        return target.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof LazyLoadingHandler<?> that)) {
            return false;
        }
        return loaded == that.loaded && Objects.equals(persistenceContext, that.persistenceContext) && Objects.equals(parentKeyHolder, that.parentKeyHolder) && Objects.equals(entityLoader, that.entityLoader) && Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(persistenceContext, parentKeyHolder, entityLoader, target, loaded);
    }
}
