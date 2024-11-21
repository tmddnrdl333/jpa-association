package persistence.sql.dml.query.builder;

import static persistence.sql.dml.query.WhereClauseGenerator.whereClause;

import java.util.ArrayList;
import java.util.List;
import persistence.sql.ddl.type.ColumnType;
import persistence.sql.dml.query.WhereCondition;

public class UpdateQueryBuilder {

    private static final String UPDATE = "update";
    private static final String SET = "set";

    private final StringBuilder queryString;

    private UpdateQueryBuilder() {
        this.queryString = new StringBuilder();
    }

    public static UpdateQueryBuilder builder() {
        return new UpdateQueryBuilder();
    }

    public String build() {
        return queryString.toString();
    }

    public UpdateQueryBuilder update(String tableName) {
        queryString.append( UPDATE )
                .append( " " )
                .append( tableName );
        return this;
    }

    public UpdateQueryBuilder set(List<String> columnNames, List<Object> columnValues) {
        queryString.append( " " )
                .append( SET )
                .append( " " )
                .append( setClause(columnNames, columnValues) );
        return this;
    }

    public UpdateQueryBuilder where(List<WhereCondition> whereConditions) {
        queryString.append( whereClause(whereConditions) );
        return this;
    }

    private String setClause(List<String> columnNames, List<Object> columnValues) {
        List<String> nameValues = new ArrayList<>();
        for (int idx = 0; idx < columnNames.size(); idx++) {
            nameValues.add( columnNames.get(idx) + " = " + format(columnValues.get(idx)) );
        }
        return String.join(", ", nameValues);
    }

    public String format(Object value) {
        if (value == null) {
            return null;
        }

        if (ColumnType.isVarcharType(value.getClass())) {
            return "'" + value + "'";
        }
        return value.toString();
    }

}
