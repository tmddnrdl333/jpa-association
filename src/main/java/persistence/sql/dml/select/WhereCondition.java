package persistence.sql.dml.select;

import java.util.Collections;
import java.util.List;

public class WhereCondition {
    private ConditionType conditionType;
    private String columnName;
    private List<String> conditions;

    public WhereCondition(String columnName, String condition) {
        this.columnName = columnName;
        this.conditions = Collections.singletonList(condition);
    }

    public ConditionType getConditionType() {
        return conditionType;
    }

    public String getColumnName() {
        return columnName;
    }

    public List<String> getConditions() {
        return conditions;
    }
}