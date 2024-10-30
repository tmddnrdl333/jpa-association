package persistence.entity;

import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.EntityTableMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntitySnapshot {
    private final Map<String, Object> columnSnapshots = new HashMap<>();

    public EntitySnapshot(Object entity) {
        final EntityTableMapper entityMapper = new EntityTableMapper(entity);
        final List<? extends ColumnDefinitionAware> columns = entityMapper.getColumnDefinitions();
        for (ColumnDefinitionAware column : columns) {
            final Object value = getNullableValue(column, entityMapper);
            columnSnapshots.put(column.getDatabaseColumnName(), value);
        }
    }

    private static Object getNullableValue(ColumnDefinitionAware column, EntityTableMapper entityMapper) {
        return entityMapper.hasValue(column) ? quoted(entityMapper.getValue(column)) : null;
    }

    private static String quoted(Object value) {
        return value instanceof String ? "'" + value + "'" : value.toString();
    }

    public boolean hasDirtyColumns(Object managedEntity) {
        final EntityTableMapper entityMapper = new EntityTableMapper(managedEntity);
        final List<? extends ColumnDefinitionAware> columns = entityMapper.getColumnDefinitions();
        return columns.stream()
                .anyMatch(column -> {
                            final Object entityValue = getNullableValue(column, entityMapper);
                            final Object snapshotValue = this.columnSnapshots.get(column.getDatabaseColumnName());
                            return !Objects.equals(entityValue, snapshotValue);
                        }
                );
    }
}
