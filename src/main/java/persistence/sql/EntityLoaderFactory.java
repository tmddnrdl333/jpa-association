package persistence.sql;

import persistence.proxy.ProxyFactory;
import persistence.sql.dml.Database;
import persistence.sql.loader.EntityLoader;

import java.util.HashMap;
import java.util.Map;

public class EntityLoaderFactory {
    private static final EntityLoaderFactory INSTANCE = new EntityLoaderFactory();

    private final Map<Class<?>, EntityLoader<?>> context = new HashMap<>();

    private EntityLoaderFactory() {
    }

    public static EntityLoaderFactory getInstance() {
        return INSTANCE;
    }

    public <T> void addLoader(Class<T> entityType, Database database, ProxyFactory proxyFactory) {
        if (context.containsKey(entityType)) {
            return;
        }

        context.put(entityType, new EntityLoader<>(entityType, database, proxyFactory));
    }

    @SuppressWarnings("unchecked")
    public <T> EntityLoader<T> getLoader(Class<T> entityType) {
        if (context.containsKey(entityType)) {
            return (EntityLoader<T>) context.get(entityType);
        }

        throw new IllegalArgumentException("EntityLoader not found for " + entityType.getName());
    }
}
