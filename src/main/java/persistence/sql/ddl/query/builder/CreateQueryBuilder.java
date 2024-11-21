package persistence.sql.ddl.query.builder;

import static persistence.sql.ddl.query.ColumnDefinition.define;
import static persistence.sql.ddl.query.TableDefinition.definePrimaryKeyColumn;
import static persistence.sql.ddl.query.TableDefinition.definePrimaryKeyConstraint;
import static persistence.validator.AnnotationValidator.isNotPresent;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import persistence.meta.ColumnMeta;
import persistence.meta.TableMeta;
import persistence.meta.PrimaryKeyConstraint;
import persistence.sql.dialect.Dialect;

public class CreateQueryBuilder {

    private static final String CREATE_TABLE = "create table";

    private final Dialect dialect;
    private final StringBuilder queryString;

    private CreateQueryBuilder(Dialect dialect) {
        this.dialect = dialect;
        this.queryString = new StringBuilder();
    }

    public static CreateQueryBuilder builder(Dialect dialect) {
        return new CreateQueryBuilder(dialect);
    }

    public String build() {
        return queryString.toString();
    }

    public CreateQueryBuilder create(Class<?> clazz) {
        TableMeta tableMeta = new TableMeta(clazz);
        List<ColumnMeta> columnMetas = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> isNotPresent(field, Id.class))
                .filter(field -> isNotPresent(field, Transient.class))
                .map(ColumnMeta::new)
                .toList();
        PrimaryKeyConstraint primaryKeyConstraint = PrimaryKeyConstraint.from(clazz);

        queryString.append( CREATE_TABLE )
                .append( " " )
                .append( tableMeta.name() )
                .append( " (" );

        queryString.append( definePrimaryKeyColumn(primaryKeyConstraint, dialect) ).append(", ");
        queryString.append(
                columnMetas.stream()
                        .map(column -> define(column, dialect))
                        .collect(Collectors.joining(", "))
        );
        queryString.append( definePrimaryKeyConstraint(primaryKeyConstraint) );

        queryString.append(")");
        return this;
    }

}
