package persistence.sql.dml.select;

import persistence.sql.component.Condition;
import persistence.sql.component.JoinCondition;
import persistence.sql.component.TableInfo;

import java.util.List;

public class SelectQueryBuilder {
    private TableInfo fromTableInfo;
    private Condition whereCondition;
    private List<JoinCondition> joinConditions;

    public SelectQueryBuilder fromTableInfo(TableInfo fromTableInfo) {
        this.fromTableInfo = fromTableInfo;
        return this;
    }

    public SelectQueryBuilder whereCondition(Condition whereCondition) {
        this.whereCondition = whereCondition;
        return this;
    }

    public SelectQueryBuilder joinConditions(List<JoinCondition> joinConditions) {
        this.joinConditions = joinConditions;
        return this;
    }

    public SelectQuery build() {
        SelectQuery selectQuery = new SelectQuery(fromTableInfo, whereCondition);
        if (joinConditions != null) {
            selectQuery.setJoinConditions(joinConditions);
        }
        return selectQuery;
    }
}
