package persistence.sql.dml.select;

import jakarta.persistence.Id;
import persistence.sql.NameUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class SelectQueryBuilder {
    private String tableName;
    private List<WhereCondition> whereConditions = new ArrayList<>();

    private SelectQueryBuilder() {
    }

    private void setTableName(String tableName) {
        this.tableName = tableName;
    }

    private void addWhereCondition(String columnName, String condition) {
        whereConditions.add(new WhereCondition(columnName, condition));
    }

    public String build() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("select * from ").append(tableName);

        if (whereConditions != null && !whereConditions.isEmpty()) {
            String whereClause = getWhereClause();
            stringBuilder.append(whereClause);
        }

        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    private String getWhereClause() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("where ");
        for (WhereCondition whereCondition : whereConditions) {
            ConditionType conditionType = whereCondition.getConditionType();
            String columnName = whereCondition.getColumnName();
            List<String> conditions = whereCondition.getConditions();
            if (conditionType != null) {
                stringBuilder.append(conditionType.getValue()).append(" ");
            }
            stringBuilder.append(columnName).append(" ");

            if (conditions.size() == 1) {
                stringBuilder.append("= ").append(conditions.get(0));
            } else {
                stringBuilder.append("in (");
                conditions.forEach(condition -> stringBuilder.append(condition).append(", "));
                stringBuilder.append(")");
            }
            stringBuilder.append(" ");
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }

    public static SelectQueryBuilder generateQuery(Class<?> entityClass) {
        SelectQueryBuilder selectQueryBuilder = new SelectQueryBuilder();
        selectQueryBuilder.setTableName(
                NameUtils.getTableName(entityClass)
        );
        return selectQueryBuilder;
    }

    public static SelectQueryBuilder generateQuery(Class<?> entityClass, Long id) {
        SelectQueryBuilder selectQueryBuilder = generateQuery(entityClass);
        Field idColumn = getIdColumn(entityClass);
        String columnName = NameUtils.getColumnName(idColumn);
        selectQueryBuilder.addWhereCondition(columnName, id.toString());
        return selectQueryBuilder;
    }

    private static Field getIdColumn(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Id.class)) {
                return field;
            }
        }
        throw new IllegalArgumentException("Inappropriate entity class!");
    }
}
