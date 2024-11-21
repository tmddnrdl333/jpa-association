package persistence.sql.dml.query;

public record WhereCondition(String name,
                             WhereOperator operator,
                             Object value) {

}
