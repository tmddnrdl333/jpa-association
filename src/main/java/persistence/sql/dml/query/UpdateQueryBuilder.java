package persistence.sql.dml.query;

import common.SqlLogger;
import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.EntityTableMapper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class UpdateQueryBuilder implements BaseQueryBuilder {

    public String build(Object entity) {
        final EntityTableMapper entityTableMapper = new EntityTableMapper(entity);
        final StringBuilder query = new StringBuilder("UPDATE ").append(entityTableMapper.getTableName());
        columnClause(
                query,
                entityTableMapper.getColumnDefinitions().stream()
                        .filter(column -> !column.isPrimaryKey())
                        .collect(
                                Collectors.toMap(
                                        ColumnDefinitionAware::getDatabaseColumnName,
                                        column -> entityTableMapper.hasValue(column) ? getQuoted(entityTableMapper.getValue(column)) : "null",
                                        (value1, value2) -> value2,
                                        LinkedHashMap::new
                                )
                        )

        );

        query.append(" WHERE ");
        query.append(entityTableMapper.getIdColumnName())
                .append(" = ")
                .append(entityTableMapper.getIdValue())
                .append(";");

        String sql = query.toString();
        SqlLogger.infoUpdate(sql);
        return sql;
    }

    public String build(EntityTableMapper parentMapper, EntityTableMapper childMapper) {
        final StringBuilder query = new StringBuilder("UPDATE ").append(childMapper.getTableName());
        columnClause(
                query,
                Map.of(
                        parentMapper.getJoinColumnName(childMapper.getEntityClass()),
                        parentMapper.getIdValue()
                )
        );

        query.append(" WHERE ");
        query.append(childMapper.getIdColumnName())
                .append(" = ")
                .append(childMapper.getIdValue())
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
