package study.sample;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class HelloHandler implements InvocationHandler {
    private static final Logger logger = Logger.getLogger(HelloHandler.class.getName());

    private final Hello hello;

    public HelloHandler(Hello hello) {
        this.hello = hello;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        logger.info("invoke method name: %s, args: %s".formatted(method.getName(), args[0]));

        Object invokeResult = method.invoke(hello, args);

        if (invokeResult instanceof String) {
            return ((String) invokeResult).toUpperCase();
        }

        return invokeResult;
    }
}
