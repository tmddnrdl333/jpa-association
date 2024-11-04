package persistence.sql.dml.impl;

import jakarta.persistence.OneToMany;
import org.jetbrains.annotations.NotNull;
import persistence.sql.QueryBuilder;
import persistence.sql.clause.Clause;
import persistence.sql.clause.JoinClause;
import persistence.sql.clause.LeftJoinClause;
import persistence.sql.common.util.NameConverter;
import persistence.sql.data.ClauseType;
import persistence.sql.data.QueryType;
import persistence.sql.dml.MetadataLoader;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class SelectQueryBuilder implements QueryBuilder {
    private final NameConverter nameConverter;

    public SelectQueryBuilder(NameConverter nameConverter) {
        this.nameConverter = nameConverter;
    }

    @Override
    public QueryType queryType() {
        return QueryType.SELECT;
    }

    @Override
    public boolean supported(QueryType queryType) {
        return QueryType.SELECT.equals(queryType);
    }

    @Override
    public String build(MetadataLoader<?> loader, Clause... clauses) {
        List<Clause> joinClauses = Clause.filterByClauseType(clauses, ClauseType.LEFT_JOIN);

        String columns = getColumnClause(loader, joinClauses);
        String tableName = loader.getTableNameWithAlias();

        StringBuilder query = new StringBuilder("SELECT %s FROM %s".formatted(columns, tableName));

        List<Clause> conditionalClauses = Clause.filterByClauseType(clauses, ClauseType.WHERE);

        if (!joinClauses.isEmpty()) {
            query.append(" ");
            query.append(joinClauses.stream()
                    .map(Clause::clause)
                    .collect(Collectors.joining(" ")));
        }

        if (!conditionalClauses.isEmpty()) {
            query.append(" WHERE ");
            query.append(getWhereClause(conditionalClauses));
        }

        return query.toString();
    }

    @NotNull
    private String getColumnClause(MetadataLoader<?> loader, List<Clause> joinClauses) {
        List<Field> originFields = loader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(OneToMany.class));
        String originColumn = originFields.stream()
                .map(field -> JoinClause.combineAlias(loader.getTableAlias(), loader.getColumnName(field, nameConverter)))
                .collect(Collectors.joining(DELIMITER));

        String joinColumns = joinClauses.stream()
                .map(clause -> ((LeftJoinClause) clause).columns())
                .collect(Collectors.joining(DELIMITER));

        return originColumn + DELIMITER + joinColumns;
    }
}
