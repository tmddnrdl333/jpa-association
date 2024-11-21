package persistence.sql.dml.query.builder;

import static persistence.sql.dml.query.WhereClauseGenerator.whereClause;

import java.util.List;
import persistence.sql.dml.query.WhereCondition;

public class DeleteQueryBuilder {

    private static final String DELETE_FROM = "delete from";
    private final StringBuilder queryString;

    private DeleteQueryBuilder() {
        this.queryString = new StringBuilder();
    }

    public static DeleteQueryBuilder builder() {
        return new DeleteQueryBuilder();
    }

    public String build() {
        return queryString.toString();
    }

    public DeleteQueryBuilder delete(String tableName) {
        queryString.append( DELETE_FROM )
                .append( " " )
                .append( tableName );
        return this;
    }

    public DeleteQueryBuilder where(List<WhereCondition> whereConditions) {
        queryString.append( whereClause(whereConditions) );
        return this;
    }

}
