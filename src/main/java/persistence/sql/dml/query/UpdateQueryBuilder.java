package persistence.sql.dml.query;

import common.SqlLogger;
import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.TableDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateQueryBuilder implements BaseQueryBuilder {

    public String build(Object entity, TableDefinition tableDefinition) {
        final StringBuilder query = new StringBuilder("UPDATE ").append(tableDefinition.getTableName());
        columnClause(
                query,
                tableDefinition.getColumns().stream()
                        .filter(column -> !column.isPrimaryKey())
                        .collect(
                                Collectors.toMap(
                                        ColumnDefinitionAware::getDatabaseColumnName,
                                        column -> tableDefinition.hasValue(entity, column)
                                                ? getQuoted(tableDefinition.getValue(entity, column)) : "null",
                                        (value1, value2) -> value2,
                                        LinkedHashMap::new
                                )
                        )

        );

        query.append(" WHERE ");
        query.append(tableDefinition.getIdColumnName())
                .append(" = ")
                .append(tableDefinition.getIdValue(entity))
                .append(";");

        String sql = query.toString();
        SqlLogger.infoUpdate(sql);
        return sql;
    }

    public String build(Object parent, Object child,
                        TableDefinition parentDefinition,
                        TableDefinition childDefinition) {

        final StringBuilder query = new StringBuilder("UPDATE ").append(childDefinition.getTableName());
        columnClause(
                query,
                Map.of(
                        parentDefinition.getJoinColumnName(childDefinition.getEntityClass()),
                        parentDefinition.getIdValue(parent)
                )
        );

        query.append(" WHERE ");
        query.append(childDefinition.getIdColumnName())
                .append(" = ")
                .append(childDefinition.getIdValue(child))
                .append(";");

        final String updateQuery = query.toString();
        SqlLogger.infoUpdate(updateQuery);
        return updateQuery;
    }

    private void columnClause(StringBuilder query, Map<String, Object> columns) {
        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("Columns cannot be null or empty");
        }

        query.append(" SET ");
        String columnClause = columns.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .reduce((column1, column2) -> column1 + ", " + column2).orElse("");
        query.append(columnClause);
    }

}
