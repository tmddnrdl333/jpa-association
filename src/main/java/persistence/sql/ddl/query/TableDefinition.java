package persistence.sql.ddl.query;

import persistence.sql.dialect.Dialect;
import persistence.meta.PrimaryKeyConstraint;

public class TableDefinition {

    public static String definePrimaryKeyColumn(PrimaryKeyConstraint primaryKeyConstraint, Dialect dialect) {
        StringBuilder builder = new StringBuilder();
        builder.append( ColumnDefinition.define(primaryKeyConstraint.column(), dialect) )
                .append( " " )
                .append( dialect.getIdentifierGenerationType(primaryKeyConstraint.generationType()) );
        return builder.toString();
    }

    public static String definePrimaryKeyConstraint(PrimaryKeyConstraint primaryKeyConstraint) {
        StringBuilder builder = new StringBuilder();
        builder.append(", ")
                .append("primary key (")
                .append(primaryKeyConstraint.column().name())
                .append(")");
        return builder.toString();
    }

}
