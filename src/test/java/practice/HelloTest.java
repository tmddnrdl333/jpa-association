package practice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;

class HelloTest {
    private Hello sut;

    @BeforeEach
    void setUp() {
        final Hello target = new HelloTarget();
        sut = (Hello) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{Hello.class},
                new HelloHandler(target)
        );
    }

    @Test
    void testUpperCaseConversion() {
        final String result = sut.sayHello("world");
        assertThat(result).isEqualTo("HELLO WORLD");
    }

    @Test
    void testUpperCaseConversionWithMixedCase() {
        final String result = sut.sayHi("World");
        assertThat(result).isEqualTo("HI WORLD");
    }

    @Test
    void testEmptyString() {
        final String result = sut.sayHello("");
        assertThat(result).isEqualTo("HELLO ");
    }

    @Test
    void testAlreadyUpperCase() {
        final String result = sut.sayThankYou("WORLD");
        assertThat(result).isEqualTo("THANK YOU WORLD");
    }
}
