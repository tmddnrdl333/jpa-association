package practice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class HelloHandler implements InvocationHandler {
    private final Hello hello;

    public HelloHandler(final Hello hello) {
        this.hello = hello;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        final String result = (String) method.invoke(hello, args);
        return result.toUpperCase();
    }
}
