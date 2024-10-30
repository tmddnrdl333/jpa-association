package persistence.sql.definition;

import persistence.sql.SqlType;

public interface ColumnDefinitionAware {
    String getDatabaseColumnName();

    String getEntityFieldName();

    boolean isNullable();

    int getLength();

    SqlType getSqlType();

    boolean isPrimaryKey();
}
