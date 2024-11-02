package persistence.entity;

import java.util.List;

public interface EntityLoader {
    <T> T load(Class<T> entityType, Object id);

    <T> List<T> loadCollection(Class<T> entityType, String columnName, Object value);
}
