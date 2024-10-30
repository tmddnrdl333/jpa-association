package persistence.sql.definition;

import jakarta.persistence.Id;
import persistence.sql.SqlType;

import java.lang.reflect.Field;

public class TableColumn implements ColumnDefinitionAware {
    private final ColumnDefinition columnDefinition;
    private final boolean isPrimaryKey;

    public TableColumn(Field field) {
        this.columnDefinition = new ColumnDefinition(field);
        this.isPrimaryKey = field.isAnnotationPresent(Id.class);
    }

    @Override
    public String getDatabaseColumnName() {
        return columnDefinition.getColumnName();
    }

    @Override
    public String getEntityFieldName() {
        return columnDefinition.getDeclaredName();
    }

    @Override
    public boolean isNullable() {
        return columnDefinition.isNullable();
    }

    @Override
    public int getLength() {
        return columnDefinition.getLength();
    }

    @Override
    public SqlType getSqlType() {
        return columnDefinition.getSqlType();
    }

    @Override
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
}
