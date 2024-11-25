package persistence.sql.dml.select;

public enum ConditionType {
    AND("and"),
    OR("or"),
    ;
    private final String value;

    ConditionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
