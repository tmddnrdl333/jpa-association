package persistence.sql.dml.query;

import common.SqlLogger;
import persistence.sql.definition.ColumnDefinitionAware;
import persistence.sql.definition.EntityTableMapper;

import java.util.List;

public class InsertQueryBuilder implements BaseQueryBuilder {
    private static final String EMPTY_STRING = "";

    public String build(Object entity) {
        final StringBuilder query = new StringBuilder();
        final EntityTableMapper entityTableMapper = new EntityTableMapper(entity);
        final List<? extends ColumnDefinitionAware> columns = entityTableMapper.hasValueColumns();

        query.append("INSERT INTO ");
        query.append(entityTableMapper.getTableName());

        query.append(" (");
        query.append(columnsClause(columns));

        query.append(") VALUES (");
        query.append(valueClause(entityTableMapper, columns));
        query.append(");");

        final String sql = query.toString();
        SqlLogger.infoInsert(sql);
        return sql;
    }

    private String columnsClause(List<? extends ColumnDefinitionAware> columns) {
        return columns
                .stream()
                .map(ColumnDefinitionAware::getDatabaseColumnName)
                .reduce((column1, column2) -> column1 + ", " + column2)
                .orElse(EMPTY_STRING);
    }

    private String valueClause(EntityTableMapper mapper, List<? extends ColumnDefinitionAware> columns) {
        return mapper.getValues(columns)
                .stream()
                .map(this::getQuoted)
                .reduce((value1, value2) -> value1 + ", " + value2)
                .orElse(EMPTY_STRING);
    }

}
