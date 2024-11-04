package persistence.sql.dml.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import persistence.sql.clause.LeftJoinClause;
import persistence.sql.clause.WhereConditionalClause;
import persistence.sql.common.util.CamelToSnakeConverter;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.fixture.TestOrder;
import persistence.sql.fixture.TestOrderItem;
import persistence.sql.fixture.TestPerson;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SelectQueryBuilder 테스트")
class SelectQueryBuilderTest {
    private final SelectQueryBuilder builder = new SelectQueryBuilder(CamelToSnakeConverter.getInstance());
    private final MetadataLoader<TestPerson> loader = new SimpleMetadataLoader<>(TestPerson.class);
    private final MetadataLoader<TestOrder> testOrderLoader = new SimpleMetadataLoader<>(TestOrder.class);

    @Test
    @DisplayName("build 함수는 SELECT 쿼리를 생성한다.")
    void testSelectQueryBuild() {
        // given
        WhereConditionalClause clause = WhereConditionalClause.builder().column("id").eq("1");

        // when
        String query = builder.build(loader, clause);

        // then
        assertThat(query).isEqualTo("SELECT id, nick_name, old, email FROM users WHERE id = 1");
    }

    @Test
    @DisplayName("build 함수는 연관관계를 고려해 SELECT 쿼리를 생성한다.")
    void testSelectQueryBuildWithAssociation() {
        // given
        WhereConditionalClause clause = WhereConditionalClause.builder().column("id").eq("1");
        LeftJoinClause leftJoinClause = LeftJoinClause.of(TestOrder.class, TestOrderItem.class);

        // when
        String query = builder.build(testOrderLoader, clause, leftJoinClause);

        // then
        assertThat(query).isEqualTo("SELECT testorder.id, " +
                "testorder.order_number, " +
                "testorder.order_items, " +
                "testorderitem.id, " +
                "testorderitem.product, " +
                "testorderitem.quantity " +
                "FROM orders testorder " +
                "LEFT JOIN order_items testorderitem ON testorder.id = testorderitem.id " +
                "WHERE id = 1");
    }
}
