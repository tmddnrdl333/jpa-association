package persistence.sql.dml.query;

import java.util.List;
import java.util.stream.Collectors;

public class WhereClauseGenerator {

    private static final String WHERE = "where";
    private static final String AND = "and";


    public static String whereClause(List<WhereCondition> whereConditions) {
        return new StringBuilder()
                .append( " " )
                .append( WHERE )
                .append(
                        whereConditions.stream()
                        .map(condition -> condition.name() + " " + condition.operator().value() + " " + condition.value())
                        .collect(Collectors.joining(AND, " ", ""))
                ).toString();
    }

}
