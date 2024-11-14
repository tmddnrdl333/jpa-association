package orm.dsl.render;

import orm.TableEntity;
import orm.TableField;
import orm.assosiation.RelationField;
import orm.assosiation.RelationFields;
import orm.dsl.condition.Conditions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 조인절이 있는 SELECT 쿼리를 생성용
 */
public class WithJoinQueryRenderer<E> extends SelectRenderer<E> {

    public WithJoinQueryRenderer(TableEntity<E> tableEntity, Conditions conditions, RelationFields<E> relationFields) {
        super(tableEntity, conditions, relationFields);
    }

    @Override
    public String renderSql() {
        StringBuilder queryBuilder = new StringBuilder();

        queryBuilder.append("SELECT %s".formatted(renderAliasAwareSelectColumn()));
        queryBuilder.append(" FROM %s".formatted(renderFrom()));

        queryBuilder.append(" ").append(renderJoin());

        // TODO: join된 테이블에 대한 조건절은 나중에 구현한다.
        if (conditions.hasCondition()) {
            queryBuilder.append(" WHERE %s".formatted(conditions.renderCondition()));
        }

        return queryBuilder.toString();
    }

    // SELECT 컬럼 렌더링
    private String renderAliasAwareSelectColumn() {

        // 드라이빙 테이블 컬럼들
        String derivingColumns = aliasedColumnNameInSingleTable(tableEntity.getAliasName(), tableEntity.getAllFields());

        // 드리븐 테이블 컬럼들
        String allDrivenColumns = relationFields.getEagerRelationList().stream()
                .map(relationField -> aliasedColumnNameInSingleTable(
                        relationField.getAliasName(), relationField.getJoinTableEntity().getAllFields())
                ).collect(Collectors.joining(","));

        return Stream.of(derivingColumns, allDrivenColumns)
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(","));
    }

    private String renderFrom() {
        return tableEntity.getTableName() + " " + tableEntity.getAliasName();
    }

    private String renderJoin() {
        List<RelationField> eagerRelationList = relationFields.getEagerRelationList();
        return eagerRelationList.stream()
                .map(relationField -> "JOIN %s %s ON %s = %s"
                        .formatted(
                                relationField.getTableName(), relationField.getAliasName(),
                                aliasAwareIdColumnName(tableEntity),
                                aliasAwareLinkedColumnName(relationField))
                )
                .collect(Collectors.joining(" "));
    }

    private String aliasAwareIdColumnName(TableEntity<?> tableEntity) {
        return "%s.%s".formatted(tableEntity.getAliasName(), tableEntity.getId().getFieldName());
    }

    private String aliasAwareLinkedColumnName(RelationField relationField) {
        TableEntity<?> joinTableEntity = relationField.getJoinTableEntity();
        return "%s.%s".formatted(joinTableEntity.getAliasName(), relationField.getJoinColumnName());
    }

    // 단일 테이블에 대한 컬럼들 매핑
    private String aliasedColumnNameInSingleTable(String aliasName, List<TableField> fields) {
        return fields.stream()
                .map(tableField -> "%s.%s AS %s_%s".formatted(
                        aliasName, tableField.getFieldName(),
                        aliasName, tableField.getFieldName()
                    )
                )
                .collect(Collectors.joining(","));
    }
}
