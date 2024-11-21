package persistence.sql.dml.query;

public enum WhereOperator {

    EQUAL("=");

    String value;

    WhereOperator(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

}
