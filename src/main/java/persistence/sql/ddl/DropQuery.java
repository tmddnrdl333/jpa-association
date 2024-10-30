package persistence.sql.ddl;

import persistence.meta.EntityTable;

public class DropQuery {
    private static final String QUERY_TEMPLATE = "DROP TABLE IF EXISTS %s";

    private final EntityTable entityTable;

    public DropQuery(Class<?> entityType) {
        this.entityTable = new EntityTable(entityType);
    }

    public String drop() {
        return QUERY_TEMPLATE.formatted(entityTable.getTableName());
    }
}
