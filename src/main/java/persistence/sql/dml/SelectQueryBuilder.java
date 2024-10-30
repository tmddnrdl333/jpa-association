package persistence.sql.dml;

import static persistence.sql.QueryConst.*;

public class SelectQueryBuilder {
    private final StringBuilder sql;

    public SelectQueryBuilder() {
        this.sql = new StringBuilder();
    }

    public SelectQueryBuilder select(String clause) {
        sql.append(SELECT_CLAUSE)
                .append(BLANK)
                .append(clause)
                .append(BLANK);
        return this;
    }

    public SelectQueryBuilder from(String clause) {
        sql.append(FROM_CLAUSE)
                .append(BLANK)
                .append(clause)
                .append(BLANK);
        return this;
    }

    public SelectQueryBuilder innerJoin(String clause) {
        sql.append(INNER_JOIN__CLAUSE)
                .append(BLANK)
                .append(clause)
                .append(BLANK);
        return this;
    }

    public SelectQueryBuilder where(String clause) {
        sql.append(WHERE_CLAUSE)
                .append(BLANK)
                .append(clause)
                .append(BLANK);
        return this;
    }

    public SelectQueryBuilder where(String leftClause, Object rightClause) {
        sql.append(WHERE_CLAUSE)
                .append(BLANK)
                .append(leftClause)
                .append(EQUAL)
                .append(getValueWithQuotes(rightClause))
                .append(BLANK);
        return this;
    }

    public SelectQueryBuilder on(String leftClause, String rightClause) {
        sql.append(ON_CLAUSE)
                .append(BLANK)
                .append(leftClause)
                .append(EQUAL)
                .append(rightClause)
                .append(BLANK);
        return this;
    }

    public String build() {
        return sql.toString().trim();
    }

    private String getValueWithQuotes(Object value) {
        if (value.getClass() == String.class) {
            return "'%s'".formatted(String.valueOf(value));
        }
        return String.valueOf(value);
    }
}
