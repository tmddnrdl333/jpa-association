package orm.dsl.render;

import orm.TableEntity;
import orm.assosiation.RelationFields;
import orm.dsl.condition.Conditions;

/**
 * SELECT 쿼리 렌더링용 추상 클래스
 */
public abstract class SelectRenderer<E> {

    protected final TableEntity<E> tableEntity;
    protected final Conditions conditions;
    protected final RelationFields<E> relationFields;

    public SelectRenderer(TableEntity<E> tableEntity, Conditions conditions, RelationFields<E> relationFields) {
        this.tableEntity = tableEntity;
        this.conditions = conditions;
        this.relationFields = relationFields;
    }

    public abstract String renderSql();
}
