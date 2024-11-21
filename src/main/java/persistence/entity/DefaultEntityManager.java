package persistence.entity;

import static persistence.entity.context.status.EntityStatus.DELETED;
import static persistence.entity.context.status.EntityStatus.GONE;
import static persistence.entity.context.status.EntityStatus.LOADING;
import static persistence.entity.context.status.EntityStatus.MANAGED;

import java.sql.Connection;
import java.util.Optional;
import jdbc.JdbcTemplate;
import persistence.entity.context.DefaultPersistenceContext;
import persistence.entity.persister.EntityPersister;
import persistence.entity.context.PersistenceContext;
import persistence.entity.persister.DefaultEntityPersister;
import persistence.entity.loader.DefaultEntityLoader;
import persistence.entity.loader.EntityLoader;

public class DefaultEntityManager implements EntityManager {

    private final JdbcTemplate jdbcTemplate;
    private final PersistenceContext context;
    private final EntityPersister persister;
    private final EntityLoader loader;

    public DefaultEntityManager(Connection connection) {
        this.jdbcTemplate = new JdbcTemplate(connection);
        this.context = new DefaultPersistenceContext();
        this.persister = new DefaultEntityPersister(jdbcTemplate);
        this.loader = new DefaultEntityLoader(jdbcTemplate);
    }

    @Override
    public <T> T find(Class<T> clazz, Object id) {
        Optional<T> entity = context.getEntity(id, clazz);
        return entity.orElseGet(() -> loadEntity(clazz, id));
    }

    private <T> T loadEntity(Class<T> clazz, Object id) {
        T loadEntity = loader.load(clazz, id);

        context.addEntityEntry(loadEntity, LOADING);
        context.addEntity(loadEntity);
        context.addDatabaseSnapshot(loadEntity);

        context.updateEntityEntry(loadEntity, MANAGED);

        return loadEntity;
    }

    @Override
    public void persist(Object entity) {
        context.addEntity(entity);

        Object saveEntity = persister.insert(entity);
        context.addEntityEntry(saveEntity, MANAGED);
    }

    @Override
    public void remove(Object entity) {
        context.updateEntityEntry(entity, DELETED);
        persister.delete(entity);
        context.updateEntityEntry(entity, GONE);
        context.removeDatabaseSnapshot(entity);
    }

    @Override
    public <T> T merge(T entity) {
        if (context.isDirty(entity)) {
            persister.update(entity);
            context.removeEntity(entity);
            context.addEntity(entity);
        }
        return entity;
    }

}
