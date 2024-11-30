package persistence.sql.dml.select;

import persistence.sql.component.ColumnInfo;
import persistence.sql.component.Condition;
import persistence.sql.component.JoinCondition;
import persistence.sql.component.TableInfo;

import java.util.List;

public class SelectQuery {
    private TableInfo fromTableInfo;
    private Condition whereCondition;
    private List<JoinCondition> joinConditions;

    public SelectQuery(TableInfo fromTableInfo, Condition whereCondition) {
        this.fromTableInfo = fromTableInfo;
        this.whereCondition = whereCondition;
    }

    public void setJoinConditions(List<JoinCondition> joinConditions) {
        this.joinConditions = joinConditions;
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("select * from ")
                .append(fromTableInfo.getTableName())
                .append(" ");
        if (whereCondition != null) {
            stringBuilder.append(getWhereClause());
        }
        if (joinConditions != null) {
            stringBuilder.append(getJoinClauses());
        }

        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(";");
        return stringBuilder.toString();
    }

    private String getWhereClause() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append("where ")
                .append(whereCondition.getColumnInfo().getColumnName())
                .append(" ");

        List<String> values = whereCondition.getValues();
        if (values.isEmpty()) {
            stringBuilder.append("= null");
        } else if (values.size() == 1) {
            stringBuilder.append("= ").append(values.get(0));
        } else {
            stringBuilder.append("in (");
            values.forEach(value -> stringBuilder.append(value).append(", "));
            stringBuilder.setLength(stringBuilder.length() - 2);
            stringBuilder.append(")");
        }
        /* todo : andCondition and orCondition */
        stringBuilder.append(" ");
        return stringBuilder.toString();
    }

    private String getJoinClauses() {
        StringBuilder stringBuilder = new StringBuilder();
        joinConditions.forEach(
                joinCondition -> stringBuilder.append(getSingleJoinClause(joinCondition))
        );
        return stringBuilder.toString();
    }

    private String getSingleJoinClause(JoinCondition joinCondition) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(joinCondition.getJoinType().getValue())
                .append(" ")
                .append(joinCondition.getTableInfo().getTableName())
                .append(" on ");
        ColumnInfo sourceColumnInfo = joinCondition.getSourceColumnInfo();
        String sourceTableName = sourceColumnInfo.getTableInfo().getTableName();
        String sourceColumnName = sourceColumnInfo.getColumnName();
        stringBuilder
                .append(sourceTableName)
                .append(".")
                .append(sourceColumnName)
                .append(" = ");
        ColumnInfo targetColumnInfo = joinCondition.getTargetColumnInfo();
        String targetTableName = targetColumnInfo.getTableInfo().getTableName();
        String targetColumnName = targetColumnInfo.getColumnName();
        stringBuilder
                .append(targetTableName)
                .append(".")
                .append(targetColumnName)
                .append(" ");
        return stringBuilder.toString();
    }
}
