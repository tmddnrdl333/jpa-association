package orm;

import orm.assosiation.RelationField;
import orm.assosiation.RelationFields;
import orm.dirty_check.DirtyCheckMarker;
import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import orm.dsl.holder.EntityIdHolder;

import java.util.List;

public class DefaultEntityPersister implements EntityPersister {

    private final QueryBuilder queryBuilder;
    private final QueryRunner queryRunner;

    public DefaultEntityPersister(QueryBuilder queryBuilder, QueryRunner queryRunner) {
        this.queryBuilder = queryBuilder;
        this.queryRunner = queryRunner;
    }

    @Override
    public <T> T persist(T entity) {
        var tableClassifier = new RelationFields<>(entity);

        T rootEntity = queryBuilder.insertIntoValues(entity, queryRunner)
                .returnAsEntity();

        // 연관관계 필드 저장
        List<RelationField> valuedRelationList = tableClassifier.getValuedRelationList();
        for (RelationField relationField : valuedRelationList) {
            persistRelations(relationField);
        }
        return rootEntity;
    }

    // 연관관계 필드 저장
    private <T> void persistRelations(RelationField relationField) {

        // 연관관계가 컬랙션이 아니면 진행하지 않음
        if (!relationField.isValueTypeCollection()) {
            return;
        }

        List<Object> entityList = relationField.getValueAsList();
        for (Object entity : entityList) {
            queryBuilder.insertIntoValues(entity, queryRunner)
                    .returnAsEntity();
        }
    }

    @Override
    public <T> T update(T entity, T oldVersion) {
        final var dirtyCheckMarker = new DirtyCheckMarker<>(entity, oldVersion);
        final var hasDirty = dirtyCheckMarker.compareAndMarkChangedField();

        if (hasDirty) {
            queryBuilder.update(dirtyCheckMarker.getEntity(), queryRunner)
                    .byId()
                    .execute();
        }

        return entity;
    }

    @Override
    public void remove(Object entity) {
        queryBuilder.deleteFrom(entity, queryRunner).byId().execute();
    }

    @Override
    public <T> T getDatabaseSnapshot(EntityIdHolder<T> idHolder) {
        return queryBuilder.selectFrom(idHolder.getEntityClass(), queryRunner)
                .findById(idHolder.getIdValue())
                .fetchOne();
    }
}
