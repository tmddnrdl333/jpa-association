package orm.dsl.step.dml;

public interface UpdateStep<E> extends WhereStep {
    ConditionStep byId();

    // 변경된 필드만 확인하고 업데이트 할지 여부
    UpdateStep<E> withBitsetAware();
}
