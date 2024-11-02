package jdbc;

import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.definition.TableDefinition;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class EagerFetchRowMapper<T> extends AbstractRowMapper<T> {
    private final TableDefinition tableDefinition;

    public EagerFetchRowMapper(Class<T> clazz) {
        super(clazz);
        this.tableDefinition = new TableDefinition(clazz);
    }

    @Override
    protected void setAssociation(ResultSet resultSet, T instance) throws NoSuchFieldException, SQLException {
        do {
            List<TableAssociationDefinition> associations = tableDefinition.getAssociations();
            if (associations.isEmpty()) {
                return;
            }

            for (TableAssociationDefinition association : associations) {
                if (!association.isEager()) {
                    continue;
                }

                final Object associatedInstance = newInstance(association.getEntityClass());
                setColumns(resultSet, association.getAssociatedTableDefinition(), associatedInstance);

                final Collection<Object> entityCollection = association.getCollectionField(instance);
                entityCollection.add(associatedInstance);
            }
        } while (resultSet.next());
    }
}
