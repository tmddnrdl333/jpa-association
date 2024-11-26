package persistence.sql.component;

public class JoinCondition {
    private JoinType joinType;
    private TableInfo tableInfo;
    private ColumnInfo onConditionColumn1;
    private ColumnInfo onConditionColumn2;

    public JoinType getJoinType() {
        return joinType;
    }

    public TableInfo getTableInfo() {
        return tableInfo;
    }

    public ColumnInfo getOnConditionColumn1() {
        return onConditionColumn1;
    }

    public ColumnInfo getOnConditionColumn2() {
        return onConditionColumn2;
    }

    public JoinCondition(JoinType joinType, TableInfo tableInfo, ColumnInfo onConditionColumn1, ColumnInfo onConditionColumn2) {
        this.joinType = joinType;
        this.tableInfo = tableInfo;
        this.onConditionColumn1 = onConditionColumn1;
        this.onConditionColumn2 = onConditionColumn2;
    }
}
