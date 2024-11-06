package persistence.proxy;

import persistence.sql.context.PersistenceContext;

import java.util.Collection;

public interface ProxyFactory {

    /**
     * Collection을 Proxy로 감싸서 반환한다.
     *
     * @param foreignKey  외래키 식별자
     * @param foreignType 외래키 참조 타입
     * @param targetClass 컬렉션에 포함된 타입
     * @param <T>         컬렉션에 포함된 제네릭 타입
     * @param <C>         컬렉션 타입
     * @return Proxy로 감싼 Collection
     */
    <T, C extends Collection<Object>> C createProxyCollection(Object foreignKey,
                                                         Class<?> foreignType,
                                                         Class<T> targetClass,
                                                         Class<C> collectionType,
                                                         PersistenceContext persistenceContext);
}
