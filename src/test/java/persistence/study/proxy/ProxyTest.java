package persistence.study.proxy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProxyTest {
    @Test
    @DisplayName("소문자를 입력하면 대문자를 반환한다.")
    public void testUpperCaseConversion() {
        // given
        final Hello hello = getProxy();

        // when
        final String helloResult = hello.sayHello("nextstep");
        final String hiResult = hello.sayHi("nextstep");
        final String thankYouResult = hello.sayThankYou("nextstep");

        // then
        assertAll(
                () -> assertThat(helloResult).isEqualTo("HELLO NEXTSTEP"),
                () -> assertThat(hiResult).isEqualTo("HI NEXTSTEP"),
                () -> assertThat(thankYouResult).isEqualTo("THANK YOU NEXTSTEP")
        );
    }

    @Test
    @DisplayName("대소문자를 혼합해서 입력하면 대문자를 반환한다.")
    public void testUpperCaseConversionWithMixedCase() {
        // given
        final Hello hello = getProxy();

        // when
        final String helloResult = hello.sayHello("nExtsTeP");
        final String hiResult = hello.sayHi("nExtsTeP");
        final String thankYouResult = hello.sayThankYou("nExtsTeP");

        // then
        assertAll(
                () -> assertThat(helloResult).isEqualTo("HELLO NEXTSTEP"),
                () -> assertThat(hiResult).isEqualTo("HI NEXTSTEP"),
                () -> assertThat(thankYouResult).isEqualTo("THANK YOU NEXTSTEP")
        );
    }

    @Test
    @DisplayName("빈 문자열을 입력하면 그대로 반환한다.")
    public void testEmptyString() {
        // given
        final Hello hello = getProxy();

        // when
        final String helloResult = hello.sayHello("");
        final String hiResult = hello.sayHi("");
        final String thankYouResult = hello.sayThankYou("");

        // then
        assertAll(
                () -> assertThat(helloResult).isEqualTo("HELLO "),
                () -> assertThat(hiResult).isEqualTo("HI "),
                () -> assertThat(thankYouResult).isEqualTo("THANK YOU ")
        );
    }

    @Test
    @DisplayName("대문자를 입력하면 대문자를 그대로 반환한다.")
    public void testAlreadyUpperCase() {
        // given
        final Hello hello = getProxy();

        // when
        final String helloResult = hello.sayHello("NEXTSTEP");
        final String hiResult = hello.sayHi("NEXTSTEP");
        final String thankYouResult = hello.sayThankYou("NEXTSTEP");

        // then
        assertAll(
                () -> assertThat(helloResult).isEqualTo("HELLO NEXTSTEP"),
                () -> assertThat(hiResult).isEqualTo("HI NEXTSTEP"),
                () -> assertThat(thankYouResult).isEqualTo("THANK YOU NEXTSTEP")
        );
    }

    private Hello getProxy() {
        return (Hello) Proxy.newProxyInstance(
                Hello.class.getClassLoader(),
                new Class[]{Hello.class},
                new HelloHandler(new HelloTarget()));
    }
}
