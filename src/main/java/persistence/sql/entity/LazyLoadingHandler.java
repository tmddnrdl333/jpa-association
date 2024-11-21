package persistence.sql.entity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

public class LazyLoadingHandler<T> implements InvocationHandler {
    private List<T> data;
    private boolean isLoaded;

    private final DataLoader<T> dataLoader;

    private LazyLoadingHandler(final DataLoader<T> dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        if (!isLoaded) {
            data = dataLoader.load();
            isLoaded = true;
        }
        return method.invoke(data, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> createProxy(final DataLoader<T> dataLoader) {
        return (List<T>) Proxy.newProxyInstance(
                LazyLoadingHandler.class.getClassLoader(),
                new Class[]{List.class},
                new LazyLoadingHandler<>(dataLoader)
        );
    }

    public interface DataLoader<T> {
        List<T> load();
    }
}
