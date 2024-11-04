package orm;

import orm.dsl.holder.EntityIdHolder;

/**
 * 실제 DB에 쿼리를 질의하는 인터페이스
 */
public interface EntityPersister {

    <T> T persist(T entity);

    <T> T update(T entity, T oldVersion);

    void remove(Object entity);

    <T> T getDatabaseSnapshot(EntityIdHolder<T> idHolder);
}
