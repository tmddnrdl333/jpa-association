# jpa-association

## 1단계 - OneToMany (FetchType.EAGER)

### 요구사항 1 - Select Join Query 만들기 (EAGER)
- 임의의 데이터를 넣어서 데이터를 가져와 보자
> Sql 쿼리 문을 수정해 보자

```java
public class CustomSelect {

}
```
- [x] Custom Select Query Builder 구현
  - [x] join table이 없는 경우 구현
  - [x] join table이 있는 경우 구현

### 요구사항 2 - Join Query 를 만들어 Entity 화 해보기
> FetchType.EAGER 인 경우

```java
public class SimplePersistenceContext implements PersistenceContext {


}
```

### 요구사항 3 - Save 시 Insert Query
> 연관 관계에 대한 데이터 저장 시 쿼리 만들어 보기

부모 데이터가 있는 경우, 부모 데이터가 없는 경우 나누어서 구현
```java
// order 가 있다면?
Order order = new Order();

OrderItem orderItem1 = new OrderItem();
OrderItem orderItem2 = new OrderItem();

order.getOrderItems().add(orderItem1);
order.getOrderItems().add(orderItem2);
```
- [x] OrderItem 테이블을 생성할 때 order_id가 추가되어서 생성되어야 한다.
- [x] Order가 없는 경우 OrderItem에 order_id가 null로 저장된다.
- [x] Order가 있는 경우 OrderItem에 null로 저장된 후, order_id를 update한다.

## 2단계 - Proxy

### 요구사항 1 - Dynamic Proxy 연습
```java
//프록시를 적용할 인터페이스를 정의
public interface Hello {
    String sayHello(String name);

    String sayHi(String name);

    String sayThankYou(String name);
}

//인터페이스의 구현체 구현
class HelloTarget implements Hello {
  @Override
  public String sayHello(String name) {
    return "Hello " + name;
  }

  @Override
  public String sayHi(String name) {
    return "Hi " + name;
  }

  @Override
  public String sayThankYou(String name) {
    return "Thank You " + name;
  }
}

// InvocationHandler 구현하여 메서드 호출을 가로채고 원하는 방향으로 처리하기
public class HelloHandler implements InvocationHandler {
  private Hello hello;

  public HelloHandler(Hello hello) {
    this.hello = hello;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    return method.invoke(hello, args);
  }
}

// 테스트 상황에 맞는 핸들러 구현해보기
@Test
public void testUpperCaseConversion() {
  // 테스트 케이스: 소문자가 대문자로 변환되는지 확인
}

@Test
public void testUpperCaseConversionWithMixedCase() {
  // 테스트 케이스: 혼합된 대소문자가 모두 대문자로 변환되는지 확인
}

@Test
public void testEmptyString() {
  // 테스트 케이스: 빈 문자열이 그대로 반환되는지 확인
}

@Test
public void testAlreadyUpperCase() {
  // 테스트 케이스: 이미 대문자인 문자열이 그대로 반환되는지 확인
}
```
### 요구사항 2 - Proxy 활용
Dynamic Proxy 를 활용해 프록시객체와 원본 객체에 대한 비교를 알아보자
InvocationHandler 를 활용해 LazyLoading 을 구현해 보자
```java

public class LazyLoadingHandler implements InvocationHandler {
// 지연 로딩을 어떻게 구현해볼 수 있을까?
}
```

- [x] lazy loading 구현
  - [x] 프록시 초기화
    프록시 객체에 대해 실제 엔티티의 어떤 데이터에 처음 접근하려 할 때, 하이버네이트는 그때서야 데이터베이스에 쿼리를 보내 실제 엔티티 객체를 로드하고, 프록시 객체는 이 실제 엔티티 객체를 내부에 보관
