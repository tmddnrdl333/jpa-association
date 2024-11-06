package study;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import study.sample.Hello;
import study.sample.HelloHandler;
import study.sample.HelloTarget;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("HelloProxy 테스트")
public class HelloProxyTest {
    private static Hello helloProxy;

    @BeforeAll
    static void setup() {
        HelloHandler helloHandler = new HelloHandler(new HelloTarget());

        helloProxy = (Hello) Proxy.newProxyInstance(
                HelloProxyTest.class.getClassLoader(),
                new Class[]{Hello.class},
                helloHandler
        );
    }


    @Test
    @DisplayName("유효한 이름이 주어지면 대문자로 변환된 결과를 반환한다.")
    public void testUpperCaseConversion() {
        String actualHello = helloProxy.sayHello("catsbi");
        String actualHi = helloProxy.sayHi("catsbi");
        String actualThankYou = helloProxy.sayThankYou("catsbi");

        assertAll(
                () -> assertThat(actualHello).isEqualTo("HELLO CATSBI"),
                () -> assertThat(actualHi).isEqualTo("HI CATSBI"),
                () -> assertThat(actualThankYou).isEqualTo("THANK YOU CATSBI")
        );
    }

    @Test
    @DisplayName("혼합된 대소문자가 모두 대문자로 변환된다.")
    public void testUpperCaseConversionWithMixedCase() {
        String actualHello = helloProxy.sayHello("CatSbi");
        String actualHi = helloProxy.sayHi("CatSbi");
        String actualThankYou = helloProxy.sayThankYou("CatSbi");

        assertAll(
                () -> assertThat(actualHello).isEqualTo("HELLO CATSBI"),
                () -> assertThat(actualHi).isEqualTo("HI CATSBI"),
                () -> assertThat(actualThankYou).isEqualTo("THANK YOU CATSBI")
        );

    }

    @Test
    @DisplayName("빈 문자열이 주어지면 빈 문자열을 반환한다.")
    public void testEmptyString() {
        String actualHello = helloProxy.sayHello("");
        String actualHi = helloProxy.sayHi("");
        String actualThankYou = helloProxy.sayThankYou("");

        assertAll(
                () -> assertThat(actualHello).isEqualTo("HELLO "),
                () -> assertThat(actualHi).isEqualTo("HI "),
                () -> assertThat(actualThankYou).isEqualTo("THANK YOU ")
        );
    }

    @Test
    @DisplayName("이미 대문자인 경우 그대로 반환한다.")
    public void testAlreadyUpperCase() {
        String actualHello = helloProxy.sayHello("CATSBI");
        String actualHi = helloProxy.sayHi("CATSBI");
        String actualThankYou = helloProxy.sayThankYou("CATSBI");

        assertAll(
                () -> assertThat(actualHello).isEqualTo("HELLO CATSBI"),
                () -> assertThat(actualHi).isEqualTo("HI CATSBI"),
                () -> assertThat(actualThankYou).isEqualTo("THANK YOU CATSBI")
        );
    }


}
