## 과제 진행 요구사항 

## TODO

- [x] 1차 캐싱 구현  
이미 되어있으니 스킵한다.

- [x] 더티체킹 구현  
merge 사용시 더티체킹을 통해 update문을 생성하도록 해보자.  
영속성 상태는 다음단계에서 하는것 같다.

### 요구 사항 1
- 만들었던 PersistenceContext 에서 효율적인 메모리 관리를 위한 기능 구현 (1차 캐싱)

### 요구 사항 2

- 더티체킹 구현
1. 영속 컨텍스트 내에서 Entity 를 조회
2. 조회된 상태의 Entity 를 스냅샷 생성
3. 트랜잭션 커밋 후 해당 스냅샷과 현재 Entity 를 비교 (데이터베이스 커밋은 신경쓰지 않는다)
4. 다른 점을 쿼리로 생성 
- 트랜잭션의 flush 등 은 신경쓰지 않고 로직 구현에만 신경써보도록 하자 

```java
EntityManager em = entityManagerFactory.createEntityManager();
em.getTransaction().begin();

Entity entity = new Entity();
entity.setName("Sample");

em.persist(entity); // 엔티티를 영속성 컨텍스트에 저장
em.getTransaction().commit();
em.close();
```

이건 Spring Data JPA

```java

@Transactional
@Override
public <S extends T> S save(S entity) {

    Assert.notNull(entity, "Entity must not be null.");

    if (entityInformation.isNew(entity)) {
        em.persist(entity);
        return entity;
    } else {
        return em.merge(entity);
    }
}
```
