package persistence.sql;

import persistence.sql.definition.ColumnDefinitionAware;

public interface Dialect {
    String translateType(ColumnDefinitionAware columnDefinition);
}
