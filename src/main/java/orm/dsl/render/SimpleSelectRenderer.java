package orm.dsl.render;

import orm.TableEntity;
import orm.assosiation.RelationFields;
import orm.dsl.QueryRenderer;
import orm.dsl.condition.Conditions;

/**
 * 조인절 없이 단순한 SELECT 쿼리를 생성용
 */
public class SimpleSelectRenderer<E> extends SelectRenderer<E> {

    public SimpleSelectRenderer(TableEntity<E> tableEntity, Conditions conditions) {
        super(tableEntity, conditions, new RelationFields());
    }

    @Override
    public String renderSql() {
        QueryRenderer queryRenderer = new QueryRenderer();
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT %s".formatted(queryRenderer.joinColumnNamesWithComma(tableEntity.getAllFields())));
        queryBuilder.append(" FROM %s".formatted(tableEntity.getTableName()));

        if (conditions.hasCondition()) {
            queryBuilder.append(" WHERE %s".formatted(conditions.renderCondition()));
        }

        return queryBuilder.toString();
    }
}
