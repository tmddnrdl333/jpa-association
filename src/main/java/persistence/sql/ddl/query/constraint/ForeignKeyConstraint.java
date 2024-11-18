package persistence.sql.ddl.query.constraint;

public interface ForeignKeyConstraint {

    Class<?> appliedClass();

    String constraint();

}
