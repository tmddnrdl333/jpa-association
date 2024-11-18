package persistence.sql.dml.query;

public enum WhereOperation {

    EQUAL("+");

    String value;

    WhereOperation(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
