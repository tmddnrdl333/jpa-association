package persistence.sql.ddl;

import org.jetbrains.annotations.NotNull;
import persistence.sql.ddl.impl.JoinTargetDefinition;
import persistence.sql.node.FieldNode;

/**
 * 쿼리 제공자
 */
public interface JoinQuerySupplier extends Comparable<JoinQuerySupplier> {

    /**
     * 제공자 우선순위
     */
    short priority();

    /**
     * 제공자 지원 여부를 반환한다.
     *
     * @param definition 조인 대상 정의
     */
    boolean supported(JoinTargetDefinition definition);

    /**
     * 필드 노드를 기반으로 쿼리를 생성해 반환한다.
     *
     * @param definition 조인 대상 정의
     */
    String supply(JoinTargetDefinition definition);

    @Override
    default int compareTo(@NotNull JoinQuerySupplier o) {
        return Short.compare(priority(), o.priority());
    }
}
