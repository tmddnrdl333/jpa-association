package persistence.proxy.impl;

import persistence.proxy.ProxyFactory;
import persistence.sql.EntityLoaderFactory;
import persistence.sql.context.KeyHolder;
import persistence.sql.context.PersistenceContext;
import persistence.sql.loader.EntityLoader;

import java.lang.reflect.Proxy;
import java.util.Collection;

public class JdkProxyFactory implements ProxyFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T, C extends Collection<Object>> C createProxyCollection(Object foreignKey,
                                                                Class<?> foreignType,
                                                                Class<T> targetClass,
                                                                Class<C> collectionType,
                                                                PersistenceContext persistenceContext) {
        EntityLoader<T> loader = EntityLoaderFactory.getInstance().getLoader(targetClass);

        return (C) Proxy.newProxyInstance(
                collectionType.getClassLoader(),
                new Class[]{collectionType},
                new LazyLoadingHandler<>(new KeyHolder(foreignType, foreignKey), loader, persistenceContext)
        );
    }
}
