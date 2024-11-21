package sample.persistence.constraint;

public interface ForeignKeyConstraint {

    Class<?> appliedClass();

    String constraint();

}
