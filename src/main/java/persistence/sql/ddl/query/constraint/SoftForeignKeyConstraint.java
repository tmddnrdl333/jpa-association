package persistence.sql.ddl.query.constraint;

import java.lang.reflect.Field;
import persistence.sql.ddl.query.ColumnMeta;

public class SoftForeignKeyConstraint implements ForeignKeyConstraint {

    private final Class<?> appliedClass;
    private final ColumnMeta columnMeta;

    public SoftForeignKeyConstraint(Class<?> appliedClass, Field field, String columnName) {
        this(appliedClass, new ColumnMeta(field, columnName));
    }

    private SoftForeignKeyConstraint(Class<?> appliedClass, ColumnMeta columnMeta) {
        this.appliedClass = appliedClass;
        this.columnMeta = columnMeta;
    }

    @Override
    public Class<?> appliedClass() {
        return this.appliedClass;
    }

    @Override
    public String constraint() {
        return null;
    }
}
