##  OneToMany (FetchType.EAGER)

## TODO

- [X] Select Join 절 만들기
- [X] Fetcher에 연관 엔티티 추가 
- [x] Save 시 Insert Query 추가

### 요구 사항 1
- 요구 사항 1 - Select Join Query 만들기 (EAGER)
- 요구 사항 2 - Join Query 를 만들어 Entity 화 해보기
- 요구 사항 3 - Save 시 Insert Query

### 고민
- 조인 조건절이 조금 애매함  

jOOQ나 QueryDSL은 DSL을 만들기때문에 A.join(B) 이런게 쉽게 되는데  
여기서의 쿼리빌더는 DSL이 없기 때문에 A.join(B.class).on(A.id.eq(B.id)) 이런식으로 만들기가 힘들다.  
(테이블 스키마에 대응되는 메타클래스를 컴파일타임에 만들지 못하기 때문)

지금같은 상황에서 참고할만한 쿼리빌더는 수년전에 죽은 requery 정도... (이건 DSL을 만들지 않음)  
jOOQ, QueryDSL, requery 모두 지금처럼 인터페이스 다중상속 방식을 통해 DSL을 지원하는데  
requery가 제일 먼저 죽은걸 보면 결국 DSL을 만들어야할듯하다 (사실 QueryDSL도 죽긴함)  

- ManyToOne같은 나머지는 고려하지 않는다.


### 삽질기
아... 하이버네이트가 select시에 이렇게 alias를 붙이던 이유가 있었다.
id컬럼처럼 컬럼명이 같은 경우에는 어떤 테이블의 id인지 알 수 없기 때문에...

```sql
SELECT p.id AS col_0_0_,
       c.name AS col_1_0_,
       p.id AS id1_1_,
       p.locale AS locale2_1_,
       p.name AS name3_1_
FROM   Person p
INNER JOIN
       Country c
ON
    ( p.locale = c.locale )
ORDER BY p.id
```
