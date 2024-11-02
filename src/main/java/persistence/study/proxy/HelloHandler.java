package persistence.study.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class HelloHandler implements InvocationHandler {
    private final Hello hello;

    public HelloHandler(Hello hello) {
        this.hello = hello;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Object result = method.invoke(hello, args);
        if (result instanceof String) {
            return ((String) result).toUpperCase();
        }
        return result;
    }
}
