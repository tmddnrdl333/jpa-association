package orm;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import orm.dsl.holder.EntityIdHolder;

public class DefaultEntityLoader implements EntityLoader {

    private final QueryBuilder queryBuilder;
    private final QueryRunner queryRunner;

    public DefaultEntityLoader(QueryBuilder queryBuilder, QueryRunner queryRunner) {
        this.queryBuilder = queryBuilder;
        this.queryRunner = queryRunner;
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        var tableEntity = new TableEntity<>(clazz);
        if (tableEntity.hasRelationFields()) {
            return queryBuilder.selectFrom(tableEntity, queryRunner)
                    .joinAll()
                    .whereWithId(id)
                    .fetchOne();
        }

        return queryBuilder.selectFrom(tableEntity, queryRunner)
                .joinAll()
                .fetchOne();
    }

    @Override
    public <T> T find(EntityIdHolder<T> idHolder) {
        return queryBuilder.selectFrom(idHolder.getEntityClass(), queryRunner)
                .findById(idHolder.getIdValue())
                .fetchOne();
    }
}
