package persistence.sql.component;

public class JoinConditionBuilder {
    private JoinType joinType;
    private TableInfo tableInfo;
    private ColumnInfo onConditionColumn1;
    private ColumnInfo onConditionColumn2;

    public JoinConditionBuilder joinType(JoinType joinType) {
        this.joinType = joinType;
        return this;
    }

    public JoinConditionBuilder tableInfo(TableInfo tableInfo) {
        this.tableInfo = tableInfo;
        return this;
    }

    public JoinConditionBuilder onConditionColumn1(ColumnInfo onConditionColumn1) {
        this.onConditionColumn1 = onConditionColumn1;
        return this;
    }

    public JoinConditionBuilder onConditionColumn2(ColumnInfo onConditionColumn2) {
        this.onConditionColumn2 = onConditionColumn2;
        return this;
    }

    public JoinCondition build() {
        return new JoinCondition(joinType, tableInfo, onConditionColumn1, onConditionColumn2);
    }
}
