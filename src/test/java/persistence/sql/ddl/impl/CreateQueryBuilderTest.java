package persistence.sql.ddl.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import persistence.config.TestPersistenceConfig;
import persistence.sql.ddl.JoinTargetScanner;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.dml.TestEntityInitialize;
import persistence.sql.dml.impl.SimpleMetadataLoader;
import persistence.sql.fixture.TestOrder;
import persistence.sql.fixture.TestOrderItem;
import persistence.sql.holder.JoinTargetHolder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CreateQueryBuilder 테스트")
class CreateQueryBuilderTest {
    private final MetadataLoader<TestOrderItem> testOrderItemMetadataLoader = new SimpleMetadataLoader<>(TestOrderItem.class);

    @BeforeEach
    void setup() {
        TestPersistenceConfig config = TestPersistenceConfig.getInstance();
        JoinTargetScanner joinTargetScanner = config.joinTargetScanner();
        Set<JoinTargetDefinition> joinTargets = joinTargetScanner.scan("persistence.sql.fixture");
        JoinTargetHolder holder = JoinTargetHolder.getInstance();
        for (JoinTargetDefinition joinTarget : joinTargets) {
            holder.add(joinTarget);
        }

    }


    @Test
    @DisplayName("build 함수는 CREATE TABLE 쿼리를 생성한다.")
    void testCreateQueryBuild() {

        // given
        CreateQueryBuilder builder = CreateQueryBuilder.createDefault();

        // when
        String query = builder.build(testOrderItemMetadataLoader);

        // then
        System.out.println("query = " + query);
    }

}
