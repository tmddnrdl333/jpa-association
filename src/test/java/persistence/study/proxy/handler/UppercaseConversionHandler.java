package persistence.study.proxy.handler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class UppercaseConversionHandler implements InvocationHandler {
    private final Object target;

    public UppercaseConversionHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object result = method.invoke(target, args);
        if (result instanceof String) {
            return ((String) result).toUpperCase();
        } else {
            return result;
        }
    }
}
