package persistence.sql.ddl.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AnnotatedJoinTargetScanner 테스트")
class AnnotatedJoinTargetScannerTest {

    @Test
    @DisplayName("scan 함수는 basePackage 경로를 기반으로 JoinTargetDefinition 집합을 반환한다.")
    void scan() {
        // given
        AnnotatedJoinTargetScanner scanner = new AnnotatedJoinTargetScanner();

        // when
        Set<JoinTargetDefinition> joinTargetDefinitions = scanner.scan("persistence.sql.fixture");

        for (JoinTargetDefinition joinTargetDefinition : joinTargetDefinitions) {
            System.out.println("joinTargetDefinition = " + joinTargetDefinition);
        }
    }

}
