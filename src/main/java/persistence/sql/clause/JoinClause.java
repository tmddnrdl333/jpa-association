package persistence.sql.clause;

public interface JoinClause extends Clause {
    String JOIN_QUERY_FORMAT = "LEFT JOIN %s ON %s = %s";

    String columns();

    String table();

    String leftColumn();

    String rightColumn();

    @Override
    default String column() {
        return "";
    }

    @Override
    default String value() {
        return "";
    }

    @Override
    default String clause() {
        return JOIN_QUERY_FORMAT.formatted(table(), leftColumn(), rightColumn());
    }

    static String combineAlias(String alias, String column) {
        return alias + "." + column;
    }
}
