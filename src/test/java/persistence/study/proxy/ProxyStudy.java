package persistence.study.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.study.proxy.handler.UppercaseConversionHandler;

import java.lang.reflect.Proxy;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProxyStudy {

    @Test
    void testProxy() {
        Map proxyInstance = (Map) Proxy.newProxyInstance(
                ProxyStudy.class.getClassLoader(),
                new Class[]{Map.class},
                new DynamicInvocationHandler()
        );

        proxyInstance.put("hello", "world");
    }


    @Test
    @DisplayName("소문자가 대문자로 변환되는지 확인")
    void testUpperCaseConversion() {

        Hello hello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseConversionHandler(new HelloTarget())
        );

        assertThat(hello.sayHello("world")).isEqualTo("HELLO WORLD");
        assertThat(hello.sayHi("world")).isEqualTo("HI WORLD");
        assertThat(hello.sayThankYou("world")).isEqualTo("THANK YOU WORLD");
    }

    @Test
    @DisplayName("혼합된 대소문자가 모두 대문자로 변환되는지 확인")
    void testUpperCaseConversionWithMixedCase() {
        Hello hello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseConversionHandler(new HelloTarget())
        );

        assertThat(hello.sayHello("World")).isEqualTo("HELLO WORLD");
        assertThat(hello.sayHi("World")).isEqualTo("HI WORLD");
        assertThat(hello.sayThankYou("World")).isEqualTo("THANK YOU WORLD");
    }

    @Test
    void testEmptyString() {
        Hello hello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseConversionHandler(new HelloTarget())
        );

        assertThat(hello.sayHello("")).isEqualTo("HELLO ");
        assertThat(hello.sayHi("")).isEqualTo("HI ");
        assertThat(hello.sayThankYou("")).isEqualTo("THANK YOU ");

    }

    @Test
    void testAlreadyUpperCase() {
        Hello hello = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new UppercaseConversionHandler(new HelloTarget())
        );

        assertThat(hello.sayHello("WORLD")).isEqualTo("HELLO WORLD");
        assertThat(hello.sayHi("WORLD")).isEqualTo("HI WORLD");
        assertThat(hello.sayThankYou("WORLD")).isEqualTo("THANK YOU WORLD");
    }

}
