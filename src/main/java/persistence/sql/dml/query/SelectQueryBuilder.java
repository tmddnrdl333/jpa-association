package persistence.sql.dml.query;

import common.AliasRule;
import common.SqlLogger;
import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.definition.TableDefinition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

public class SelectQueryBuilder implements BaseQueryBuilder {
    private final StringBuilder query = new StringBuilder();
    private final TableDefinition tableDefinition;
    private final List<String> columns = new ArrayList<>();
    private final Map<String, String> conditions = new HashMap<>();

    private TableDefinition joinTableDefinition;
    private final List<String> joinTableColumns = new ArrayList<>();

    public SelectQueryBuilder(Class<?> entityClass) {
        final TableDefinition tableDefinition = new TableDefinition(entityClass);
        this.tableDefinition = tableDefinition;
        tableDefinition.getColumns().forEach(column -> {
                    columns.add(column.getDatabaseColumnName());
                }
        );
    }

    public SelectQueryBuilder join(TableAssociationDefinition tableAssociationDefinition) {
        final TableDefinition joinTableDefinition = tableAssociationDefinition.getAssociatedTableDefinition();
        this.joinTableDefinition = new TableDefinition(joinTableDefinition.getEntityClass());

        this.joinTableDefinition.getColumns().forEach(column -> {
                    joinTableColumns.add(column.getDatabaseColumnName());
                }
        );
        return this;
    }

    public SelectQueryBuilder where(String column, String value) {
        conditions.put(column, value);
        return this;
    }

    private void selectClause() {
        query.append("SELECT ")
                .append(columnsClause())
                .append(" FROM ")
                .append(tableDefinition.getTableName());
    }

    private String columnsClause() {
        final StringJoiner joiner = new StringJoiner(", ");

        columns.forEach(column -> {
            final String aliased = AliasRule.buildWith(tableDefinition.getTableName(), column);
            joiner.add(tableDefinition.getTableName() + "." + column + " AS " + aliased);
        });

        joinTableColumns.forEach(column -> {
            final String aliased = AliasRule.buildWith(joinTableDefinition.getTableName(), column);
            joiner.add(joinTableDefinition.getTableName() + "." + column + " AS " + aliased);
        });

        return joiner.toString();
    }

    private void joinClause() {
        if (joinTableDefinition != null) {
            query.append(" LEFT JOIN ")
                    .append(joinTableDefinition.getTableName())
                    .append(" ON ")
                    .append(joinTableDefinition.getTableName())
                    .append(".")
                    .append(tableDefinition.getJoinColumnName(joinTableDefinition.getEntityClass()))
                    .append(" = ")
                    .append(tableDefinition.getTableName())
                    .append(".")
                    .append(tableDefinition.getIdColumnName());
        }
    }

    private void whereClause() {
        if (conditions.isEmpty()) {
            return;
        }
        final StringJoiner joiner = new StringJoiner(" AND ");

        query.append(" WHERE ");
        conditions.forEach((column, value) -> {
            joiner.add(tableDefinition.getTableName() + "." + column + " = " + getQuoted(value));
        });
        query.append(joiner);
    }

    private void whereByIdClause(Serializable id) {
        query
                .append(" WHERE ")
                .append(tableDefinition.getTableName())
                .append(".")
                .append(tableDefinition.getIdColumnName())
                .append(" = ")
                .append(getQuoted(id)).append(";");
    }

    public String buildById(Serializable id) {
        selectClause();
        joinClause();
        whereByIdClause(id);

        final String sql = query.toString();
        SqlLogger.infoSelect(sql);
        return sql;
    }

    public String build() {
        selectClause();
        whereClause();

        final String sql = query.toString();
        SqlLogger.infoSelect(sql);
        return sql;
    }

}
