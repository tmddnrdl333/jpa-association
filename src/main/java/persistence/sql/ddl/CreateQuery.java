package persistence.sql.ddl;

import persistence.dialect.Dialect;
import persistence.meta.EntityColumn;
import persistence.meta.EntityTable;
import persistence.meta.JavaTypeConvertor;

import java.util.List;
import java.util.stream.Collectors;

import static persistence.sql.QueryConst.*;

public class CreateQuery {
    private static final String QUERY_TEMPLATE = "CREATE TABLE %s (%s)";
    private static final String NOT_NULL_COLUMN_DEFINITION = "NOT NULL";
    private static final String GENERATION_COLUMN_DEFINITION = "AUTO_INCREMENT";
    private static final String PRIMARY_KEY_COLUMN_DEFINITION = "PRIMARY KEY";

    private final EntityTable entityTable;
    private final Dialect dialect;

    public CreateQuery(Class<?> entityType, Dialect dialect) {
        this.entityTable = new EntityTable(entityType);
        this.dialect = dialect;
    }

    public String create() {
        return QUERY_TEMPLATE.formatted(entityTable.getTableName(), getColumnClause());
    }

    public String create(Class<?> parentEntityType) {
        return QUERY_TEMPLATE.formatted(entityTable.getTableName(), getColumnClause(parentEntityType));
    }

    private String getColumnClause() {
        final List<String> columnDefinitions = entityTable.getEntityColumns()
                .stream()
                .filter(this::isAvailable)
                .map(this::getColumnDefinition)
                .collect(Collectors.toList());

        return String.join(COLUMN_DELIMITER, columnDefinitions);
    }

    private Object getColumnClause(Class<?> parentEntityType) {
        final List<String> columnDefinitions = entityTable.getEntityColumns()
                .stream()
                .filter(this::isAvailable)
                .map(this::getColumnDefinition)
                .collect(Collectors.toList());

        final EntityTable parentEntityTable = new EntityTable(parentEntityType);
        if (parentEntityTable.getJoinColumnType() != entityTable.getType()) {
            throw new IllegalArgumentException();
        }

        columnDefinitions.add(
                getForeignColumnDefinition(parentEntityTable.getJoinEntityColumn(), parentEntityTable.getIdEntityColumn()));

        return String.join(COLUMN_DELIMITER, columnDefinitions);
    }

    private boolean isAvailable(EntityColumn entityColumn) {
        return !entityColumn.isOneToManyAssociation();
    }

    private String getColumnDefinition(EntityColumn entityColumn) {
        String columDefinition = entityColumn.getColumnName() + BLANK + getDbType(entityColumn);

        if (entityColumn.isNotNull()) {
            columDefinition += BLANK + NOT_NULL_COLUMN_DEFINITION;
        }

        if (entityColumn.isGenerationValue()) {
            columDefinition += BLANK + GENERATION_COLUMN_DEFINITION;
        }

        if (entityColumn.isId()) {
            columDefinition += BLANK + PRIMARY_KEY_COLUMN_DEFINITION;
        }

        return columDefinition;
    }

    private String getForeignColumnDefinition(EntityColumn parentJoinEntityColumn, EntityColumn parentIdEntityColumn) {
        return parentJoinEntityColumn.getColumnName() + BLANK + getDbType(parentIdEntityColumn);
    }

    private String getDbType(EntityColumn entityColumn) {
        final int sqlType = new JavaTypeConvertor().getSqlType(entityColumn.getType());
        final String dbTypeName = dialect.getDbTypeName(sqlType);
        final int columnLength = entityColumn.getColumnLength();

        if (columnLength == 0) {
            return dbTypeName;
        }
        return "%s(%s)".formatted(dbTypeName, columnLength);
    }
}
