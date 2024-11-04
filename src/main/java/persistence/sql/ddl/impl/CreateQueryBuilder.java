package persistence.sql.ddl.impl;

import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;
import persistence.sql.QueryBuilder;
import persistence.sql.clause.Clause;
import persistence.sql.common.util.CamelToSnakeConverter;
import persistence.sql.common.util.NameConverter;
import persistence.sql.data.QueryType;
import persistence.sql.ddl.JoinQuerySupplier;
import persistence.sql.ddl.QueryColumnSupplier;
import persistence.sql.ddl.QueryConstraintSupplier;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.holder.JoinTargetHolder;
import persistence.sql.node.FieldNode;

import java.util.*;
import java.util.stream.Collectors;

public class CreateQueryBuilder implements QueryBuilder {
    private final NameConverter nameConverter;
    private final SortedSet<QueryColumnSupplier> columnQuerySuppliers;
    private final SortedSet<JoinQuerySupplier> joinQuerySuppliers;
    private final SortedSet<QueryConstraintSupplier> constraintQuerySuppliers;

    public CreateQueryBuilder(NameConverter nameConverter,
                              SortedSet<QueryColumnSupplier> columnQuerySuppliers,
                              SortedSet<JoinQuerySupplier> joinQuerySuppliers,
                              SortedSet<QueryConstraintSupplier> constraintQuerySuppliers) {
        this.nameConverter = nameConverter;
        this.columnQuerySuppliers = columnQuerySuppliers;
        this.joinQuerySuppliers = joinQuerySuppliers;
        this.constraintQuerySuppliers = constraintQuerySuppliers;
    }

    public static CreateQueryBuilder createDefault() {
        SortedSet<QueryColumnSupplier> columnQuerySuppliers = new TreeSet<>();
        SortedSet<JoinQuerySupplier> joinQuerySuppliers = new TreeSet<>();
        SortedSet<QueryConstraintSupplier> constraintQuerySuppliers = new TreeSet<>();
        H2Dialect dialect = H2Dialect.create();

        columnQuerySuppliers.add(new ColumnNameSupplier((short) 1, CamelToSnakeConverter.getInstance()));
        columnQuerySuppliers.add(new H2ColumnTypeSupplier((short) 2, dialect));
        columnQuerySuppliers.add(new ColumnGeneratedValueSupplier((short) 3));
        columnQuerySuppliers.add(new ColumnOptionSupplier((short) 4));

        joinQuerySuppliers.add(new JoinColumnNameSupplier((short) 1, CamelToSnakeConverter.getInstance()));
        joinQuerySuppliers.add(new JoinH2ColumnTypeSupplier((short) 2, dialect));

        constraintQuerySuppliers.add(new ConstraintPrimaryKeySupplier((short) 1, CamelToSnakeConverter.getInstance()));

        return new CreateQueryBuilder(CamelToSnakeConverter.getInstance(), columnQuerySuppliers, joinQuerySuppliers, constraintQuerySuppliers);
    }

    @Override
    public QueryType queryType() {
        return QueryType.CREATE;
    }

    @Override
    public boolean supported(QueryType queryType) {
        return QueryType.CREATE.equals(queryType);
    }

    @Override
    public String build(MetadataLoader<?> loader, Clause... clauses) {
        String tableName = loader.getTableName();
        String columns = getColumnClause(loader);
        String constraints = getConstraintClause(loader);

        return "CREATE TABLE %s (%s, %S);".formatted(nameConverter.convert(tableName), columns, constraints);
    }

    private String getColumnClause(MetadataLoader<?> loader) {
        List<String> columnQueries = loader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(Transient.class)
                        && !field.isAnnotationPresent(OneToMany.class))
                .stream().map(FieldNode::from)
                .map(this::buildColumnQuery)
                .collect(Collectors.toCollection(ArrayList::new));

        Set<JoinTargetDefinition> joinTargetDefinitions = JoinTargetHolder.getInstance().get(loader.getEntityType());
        if (joinTargetDefinitions == null || joinTargetDefinitions.isEmpty()) {
            return String.join(", ", columnQueries);
        }

        joinTargetDefinitions.stream().map(this::buildJoinQuery)
                .forEach(columnQueries::add);

        return String.join(", ", columnQueries);
    }

    private String buildJoinQuery(JoinTargetDefinition definition) {
        return joinQuerySuppliers.stream().filter(supplier -> supplier.supported(definition))
                .map(supplier -> supplier.supply(definition).trim()).collect(Collectors.joining(" "));
    }

    private String buildColumnQuery(FieldNode fieldNode) {
        return columnQuerySuppliers.stream().filter(supplier -> supplier.supported(fieldNode))
                .map(supplier -> supplier.supply(fieldNode).trim()).collect(Collectors.joining(" "));
    }

    private String getConstraintClause(MetadataLoader<?> loader) {
        return loader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(Transient.class) &&
                        !field.isAnnotationPresent(OneToMany.class))
                .stream().map(FieldNode::from)
                .map(this::buildConstraintQuery)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(", "));
    }

    private String buildConstraintQuery(FieldNode fieldNode) {
        List<QueryConstraintSupplier> filteredSuppliers = constraintQuerySuppliers.stream()
                .filter(supplier -> supplier.supported(fieldNode)).toList();

        if (filteredSuppliers.isEmpty()) {
            return "";
        }

        return filteredSuppliers.stream()
                .map(supplier -> supplier.supply(fieldNode).trim())
                .collect(Collectors.joining(" , "));
    }
}
