package persistence.sql.dml.query;

import persistence.sql.definition.TableDefinition;

public class DeleteQueryBuilder {

    public String build(Object entity) {
        final StringBuilder query = new StringBuilder();
        final TableDefinition tableDefinition = new TableDefinition(entity.getClass());

        query.append("DELETE FROM ");
        query.append(tableDefinition.getTableName());

        query.append(" WHERE ");
        query.append(tableDefinition.getIdColumnName())
                .append(" = ")
                .append(tableDefinition.getIdValue(entity)).append(";");
        return query.toString();
    }
}
