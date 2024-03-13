package persistence.sql.dml;

import persistence.entity.EntityId;
import persistence.sql.model.Table;

public class ByIdQueryBuilder {

    private final static String BY_ID_QUERY_FORMAT = "WHERE %s";

    private final Table table;
    private final EntityId id;

    public ByIdQueryBuilder(Table table, EntityId id) {
        this.table = table;
        this.id = id;
    }

    public String build() {
        return buildWhereClause();
    }

    private String buildWhereClause() {
        StringBuilder whereClauseBuilder = new StringBuilder();

        String tableName = table.getName();
        whereClauseBuilder.append(tableName + '.');

        String pkColumnName = table.getPKColumnName();
        whereClauseBuilder.append(pkColumnName)
                .append('=')
                .append(id);

        return whereClauseBuilder.toString();
    }
}