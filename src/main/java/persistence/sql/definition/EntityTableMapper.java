package persistence.sql.definition;

import common.ReflectionFieldAccessUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class EntityTableMapper {
    private final Object entity;
    private final TableDefinition tableDefinition;

    public EntityTableMapper(Object entity) {
        this.entity = entity;
        this.tableDefinition = new TableDefinition(entity.getClass());
    }

    public Object getEntity() {
        return entity;
    }

    public Serializable getIdValue() {
        final TableId tableId = tableDefinition.getTableId();
        final Object id = getValue(tableId);

        if (id instanceof Serializable) {
            return (Serializable) id;
        }

        return null;
    }

    public String getTableName() {
        return tableDefinition.getTableName();
    }

    public boolean hasValue(ColumnDefinitionAware column) {
        final Object value = getValue(column);
        return value != null;
    }

    public boolean hasId() {
        return getIdValue() != null;
    }

    public String getIdColumnName() {
        return tableDefinition.getIdColumnName();
    }

    public String getIdFieldName() {
        return tableDefinition.getIdFieldName();
    }

    public Object getValue(ColumnDefinitionAware column) {
        for (Field declaredField : tableDefinition.getEntityClass().getDeclaredFields()) {
            if (declaredField.getName().equals(column.getEntityFieldName())) {
                return ReflectionFieldAccessUtils.accessAndGet(entity, declaredField);
            }
        }

        return null;
    }

    public Object getValue(String databaseColumnName) {
        for (ColumnDefinitionAware column : tableDefinition.getColumns()) {
            if (column.getDatabaseColumnName().equals(databaseColumnName)) {
                return getValue(column);
            }
        }
        
        return null;
    }

    public List<? extends ColumnDefinitionAware> hasValueColumns() {
        return tableDefinition.getColumns().stream()
                .filter(this::hasValue)
                .toList();
    }

    public List<? extends ColumnDefinitionAware> getColumnDefinitions() {
        return tableDefinition.getColumns();
    }

    public boolean hasAssociation() {
        return tableDefinition.hasAssociations();
    }

    public TableAssociationDefinition getAssociation(Class<?> associatedEntityClass) {
        return tableDefinition.getAssociation(associatedEntityClass);
    }

    public List<TableAssociationDefinition> getAssociations() {
        return tableDefinition.getAssociations();
    }

    public String getJoinColumnName(Class<?> associatedEntityClass) {
        return tableDefinition.getJoinColumnName(associatedEntityClass);
    }

    public Class<?> getEntityClass() {
        return tableDefinition.getEntityClass();
    }

    public List<Object> getValues(List<? extends ColumnDefinitionAware> columns) {
        return columns.stream()
                .map(this::getValue)
                .toList();
    }

    public Collection<?> getIterableAssociatedValue(TableAssociationDefinition association) {
        try {
            Field field = tableDefinition.getEntityClass().getDeclaredField(association.getFieldName());
            if (!Collection.class.isAssignableFrom(field.getType())) {
                return Collections.emptyList();
            }
            return (Collection<?>) ReflectionFieldAccessUtils.accessAndGet(entity, field);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }
}
