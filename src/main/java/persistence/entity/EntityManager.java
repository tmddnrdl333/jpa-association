package persistence.entity;

public interface EntityManager {
    <T> T find(Class<T> clazz, Object id);

    void persist(Object entity);

    void persist(Object entity, Object parentEntity);

    void remove(Object entity);

    void flush();

    void clear();
}
