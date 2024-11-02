package persistence.entity;

import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.TableDefinition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntitySnapshot {
    private final Map<String, Object> columnSnapshots = new HashMap<>();

    public EntitySnapshot(Object entity) {
        TableDefinition tableDefinition = new TableDefinition(entity.getClass());
        final List<? extends ColumnDefinitionAware> columns = tableDefinition.getColumns();

        for (ColumnDefinitionAware column : columns) {
            final Object value = getNullableValue(column, tableDefinition, entity);
            columnSnapshots.put(column.getDatabaseColumnName(), value);
        }
    }

    private static Object getNullableValue(ColumnDefinitionAware column, TableDefinition tableDefinition, Object entity) {
        return tableDefinition.hasValue(entity, column) ? quoted(tableDefinition.getValue(entity, column)) : null;
    }

    private static String quoted(Object value) {
        return value instanceof String ? "'" + value + "'" : value.toString();
    }

    public boolean hasDirtyColumns(Object managedEntity) {
        final TableDefinition tableDefinition = new TableDefinition(managedEntity.getClass());
        final List<? extends ColumnDefinitionAware> columns = tableDefinition.getColumns();
        return columns.stream()
                .anyMatch(column -> {
                            final Object entityValue = getNullableValue(column, tableDefinition, managedEntity);
                            final Object snapshotValue = this.columnSnapshots.get(column.getDatabaseColumnName());
                            return !Objects.equals(entityValue, snapshotValue);
                        }
                );
    }
}
