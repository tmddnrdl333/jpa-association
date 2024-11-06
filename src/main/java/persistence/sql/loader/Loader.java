package persistence.sql.loader;

import persistence.sql.dml.MetadataLoader;

import java.util.List;

public interface Loader<T> {

    T load(Object primaryKey);

    T loadByForeignKey(Object key, MetadataLoader<?> foreignLoader);

    List<T> loadAllByForeignKey(Object foreignKey, MetadataLoader<?> foreignLoader);

    List<T> loadAll();
}
