package jdbc;

import common.AliasRule;
import common.ReflectionFieldAccessUtils;
import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.definition.TableDefinition;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class EntityRowMapper<T> implements RowMapper<T> {
    private final Class<T> clazz;
    private final TableDefinition tableDefinition;

    public EntityRowMapper(Class<T> clazz) {
        this.clazz = clazz;
        this.tableDefinition = new TableDefinition(clazz);
    }

    @Override
    public T mapRow(ResultSet resultSet) throws SQLException {
        try {
            final T instance = (T) newInstance(clazz);

            for (ColumnDefinitionAware column : tableDefinition.getColumns()) {
                final String databaseColumnName = column.getDatabaseColumnName();
                final Field objectDeclaredField = clazz.getDeclaredField(column.getEntityFieldName());

                ReflectionFieldAccessUtils.accessAndSet(instance, objectDeclaredField,
                        resultSet.getObject(AliasRule.buildWith(tableDefinition.getTableName(), databaseColumnName))
                );
            }

            do {
                List<TableAssociationDefinition> associations = tableDefinition.getAssociations();
                if (associations.isEmpty()) {
                    return instance;
                }

                for (TableAssociationDefinition association : associations) {
                    final Class<?> associatedEntityClass = association.getEntityClass();
                    final Object associatedInstance = newInstance(associatedEntityClass);
                    if (association.isFetchEager()) {
                        for (ColumnDefinitionAware column : association.getAssociatedTableDefinition().getColumns()) {
                            final String databaseColumnName = column.getDatabaseColumnName();
                            final Field objectDeclaredField = associatedEntityClass.getDeclaredField(column.getEntityFieldName());

                            ReflectionFieldAccessUtils.accessAndSet(associatedInstance, objectDeclaredField,
                                    resultSet.getObject(AliasRule.buildWith(association.getAssociatedTableDefinition().getTableName(),
                                            databaseColumnName))
                            );
                        }

                        final Collection<Object> entityCollection = association.getCollectionField(instance);
                        entityCollection.add(associatedInstance);
//                        continue;
                    }

                    // TODO setProxyCollection

                }
            } while (resultSet.next());
            return instance;
        } catch (ReflectiveOperationException e) {
            throw new SQLException("Failed to map row to " + clazz.getName(), e);
        }
    }

    public Object newInstance(Class<?> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot create new instance of " + clazz.getName(), e);
        }
    }

}
