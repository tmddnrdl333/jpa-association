package persistence.entity.loader;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class LazyLoadingHandler<T> implements InvocationHandler {

    private final Class<T> clazz;
    private final Object id;
    private final EntityLoader entityLoader;
    private T target;
    private boolean isLoaded = false;

    public LazyLoadingHandler(Class<T> clazz, Object id, EntityLoader entityLoader) {
        this.clazz = clazz;
        this.id = id;
        this.entityLoader = entityLoader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (!isLoaded) {
            System.out.println("Loading data for class: " + clazz.getSimpleName() + " with ID: " + id);
            target = entityLoader.load(clazz, id);
            isLoaded = true;
        }

        return method.invoke(target, args);
    }


    public static <T> T createProxy(Class<T> clazz, Object id, EntityLoader entityLoader) {
        return (T) Proxy.newProxyInstance(
                clazz.getClassLoader(),
                new Class[]{clazz},
                new LazyLoadingHandler<>(clazz, id, entityLoader)
        );
    }

}
