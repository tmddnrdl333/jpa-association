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
        return queryBuilder.selectFrom(clazz, queryRunner)
                .findById(id)
                .fetchOne();
    }

    @Override
    public <T> T find(EntityIdHolder<T> idHolder) {
        return queryBuilder.selectFrom(idHolder.getEntityClass(), queryRunner)
                .findById(idHolder.getIdValue())
                .fetchOne();
    }
}
