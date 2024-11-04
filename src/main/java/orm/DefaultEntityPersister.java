package orm;

import orm.dirty_check.DirtyCheckMarker;
import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import orm.dsl.holder.EntityIdHolder;

public class DefaultEntityPersister implements EntityPersister {

    private final QueryBuilder queryBuilder;
    private final QueryRunner queryRunner;

    public DefaultEntityPersister(QueryBuilder queryBuilder, QueryRunner queryRunner) {
        this.queryBuilder = queryBuilder;
        this.queryRunner = queryRunner;
    }

    @Override
    public <T> T persist(T entity) {
        return queryBuilder.insertIntoValues(entity, queryRunner)
                .returnAsEntity();
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
