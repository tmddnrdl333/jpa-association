package persistence.entity.proxy;

import persistence.entity.EntityLoader;

import java.lang.reflect.Proxy;
import java.util.List;

public class ProxyFactory {
    public <T> List<T> createProxy(EntityLoader entityLoader, Class<?> entityType, Object parentEntity) {
        return (List<T>) Proxy.newProxyInstance(
                List.class.getClassLoader(),
                new Class[]{List.class},
                new LazyLoadingHandler(entityLoader, entityType, parentEntity));
    }
}
