package orm;

import orm.dsl.holder.EntityIdHolder;

public interface EntityLoader {
    <T> T find(Class<T> clazz, Object id);

    <T> T find(EntityIdHolder<T> idHolder);
}
