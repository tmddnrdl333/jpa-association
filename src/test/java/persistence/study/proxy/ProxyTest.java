package persistence.study.proxy;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ProxyTest {

    @Test
    @DisplayName("[성공] 소문자가 대문자로 변환되는지 확인")
    public void testUpperCaseConversion() {
        TextInterface proxy = newProxyInstance(new TargetTextInterface("abc"));
        assertThat(proxy.getValue()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("[성공] 혼합된 대소문자가 모두 대문자로 변환되는지 확인")
    public void testUpperCaseConversionWithMixedCase() {
        TextInterface proxy = newProxyInstance(new TargetTextInterface("aBc"));
        assertThat(proxy.getValue()).isEqualTo("ABC");
    }

    @Test
    @DisplayName("[성공] 빈 문자열이 그대로 반환되는지 확인")
    public void testEmptyString() {
        TextInterface proxy = newProxyInstance(new TargetTextInterface(""));
        assertThat(proxy.getValue()).isBlank();
    }

    @Test
    @DisplayName("[성공] 이미 대문자인 문자열이 그대로 반환되는지 확인")
    public void testAlreadyUpperCase() {
        TextInterface proxy = newProxyInstance(new TargetTextInterface("ABC"));
        assertThat(proxy.getValue()).isEqualTo("ABC");
    }

    @NotNull
    private TextInterface newProxyInstance(TextInterface textInterface) {
        return (TextInterface) Proxy.newProxyInstance(
                TextInterface.class.getClassLoader(),
                new Class[] { TextInterface.class },
                new TextUppercaseProxy(textInterface)
        );
    }

    private interface TextInterface {

        String getValue();

    }

    private static class TargetTextInterface implements TextInterface {

        private String value;

        public TargetTextInterface(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return this.value;
        }

    }

    private static class TextUppercaseProxy implements InvocationHandler {

        private final TextInterface textInterface;

        public TextUppercaseProxy(TextInterface textInterface) {
            this.textInterface = textInterface;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Object value = method.invoke(textInterface, args);

            if (value instanceof String) {
                return ((String) value).toUpperCase();
            }

            return value;
        }

    }

}
