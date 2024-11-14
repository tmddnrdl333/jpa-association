package orm.dsl.step.dml;


public interface SelectFromStep<E> extends InnerJoinForFetchStep<E> {
    ConditionForFetchStep<E> findById(Object id);
    ConditionForFetchStep<E> findAll();
}
