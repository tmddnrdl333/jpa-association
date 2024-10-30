package persistence.sql.dml;

import persistence.meta.EntityTable;

public class DeleteQuery {
    private static final String QUERY_TEMPLATE = "DELETE FROM %s WHERE %s";

    public String delete(Object entity) {
        final EntityTable entityTable = new EntityTable(entity);
        final Object id = entityTable.getIdValue();
        return QUERY_TEMPLATE.formatted(entityTable.getTableName(), entityTable.getWhereClause(id));
    }
}
