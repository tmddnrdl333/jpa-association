package persistence.sql;

public class QueryConst {
    private QueryConst() {
        throw new AssertionError();
    }

    public static final String SELECT_CLAUSE = "SELECT";
    public static final String FROM_CLAUSE = "FROM";
    public static final String WHERE_CLAUSE = "WHERE";
    public static final String INNER_JOIN__CLAUSE = "INNER JOIN";
    public static final String ON_CLAUSE = "ON";

    public static final String COLUMN_DELIMITER = ", ";
    public static final String COLUMN_ALIAS_DELIMITER = ".";
    public static final String TABLE_ALIAS_DELIMITER = " ";

    public static final String BLANK = " ";
    public static final String EQUAL = " = ";
}
