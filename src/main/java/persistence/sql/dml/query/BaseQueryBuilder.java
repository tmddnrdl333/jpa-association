package persistence.sql.dml.query;

public interface BaseQueryBuilder {
    default String getQuoted(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return value.toString();
    }
}
