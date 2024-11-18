package persistence.sql.dml.query.builder;

import static persistence.sql.dml.query.utils.QueryClauseGenerator.columnClause;
import static persistence.sql.dml.query.utils.QueryClauseGenerator.whereClause;

import java.util.List;
import java.util.stream.Collectors;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.metadata.ColumnName;
import persistence.sql.metadata.TableName;

public class SelectQueryBuilder {

    private static final String SELECT = "select";
    private static final String ALL_COLUMN = "*";
    private static final String FROM = "from";

    private final StringBuilder queryString;

    private SelectQueryBuilder() {
        this.queryString = new StringBuilder();
    }

    public static SelectQueryBuilder builder() {
        return new SelectQueryBuilder();
    }

    public String build() {
        return queryString.toString();
    }

    public SelectQueryBuilder select(List<ColumnName> columnNames) {
        queryString.append( SELECT )
                .append( " " )
                .append( columnClauseWithAlias(columnNames) );
        return this;
    }

    public SelectQueryBuilder select() {
        queryString.append( SELECT )
                .append( " " )
                .append( ALL_COLUMN );
        return this;
    }

    private static String columnClauseWithAlias(List<ColumnName> columnNames) {
        return columnNames.stream()
                .map(columnName -> columnName.alias().value() + "." + columnName.value())
                .collect(Collectors.joining(", "));
    }

    public SelectQueryBuilder from(TableName tableName) {
        queryString.append( " " )
                .append( FROM )
                .append( " " )
                .append( tableName.value() );
        return this;
    }

    public SelectQueryBuilder where(List<WhereCondition> whereConditions) {
        if (whereConditions.isEmpty()) {
            return this;
        }
        queryString.append( whereClause(whereConditions) );
        return this;
    }

}
