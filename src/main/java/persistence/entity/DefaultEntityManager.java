package persistence.entity;

import jdbc.JdbcTemplate;
import persistence.entity.proxy.ProxyFactory;
import persistence.meta.EntityColumn;
import persistence.meta.EntityTable;
import persistence.sql.dml.DeleteQuery;
import persistence.sql.dml.InsertQuery;
import persistence.sql.dml.SelectQuery;
import persistence.sql.dml.UpdateQuery;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DefaultEntityManager implements EntityManager {
    public static final String NOT_PERSISTABLE_STATUS_FAILED_MESSAGE = "엔티티가 영속화 가능한 상태가 아닙니다.";
    public static final String NOT_REMOVABLE_STATUS_FAILED_MESSAGE = "엔티티가 제거 가능한 상태가 아닙니다.";

    private final PersistenceContext persistenceContext;
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;

    private DefaultEntityManager(PersistenceContext persistenceContext, EntityPersister entityPersister,
                                 EntityLoader entityLoader) {
        this.persistenceContext = persistenceContext;
        this.entityPersister = entityPersister;
        this.entityLoader = entityLoader;
    }

    public static DefaultEntityManager of(JdbcTemplate jdbcTemplate) {
        return new DefaultEntityManager(
                new DefaultPersistenceContext(),
                new DefaultEntityPersister(jdbcTemplate, new InsertQuery(), new UpdateQuery(), new DeleteQuery()),
                new DefaultEntityLoader(jdbcTemplate, new SelectQuery(), new ProxyFactory())
        );
    }

    @Override
    public <T> T find(Class<T> entityType, Object id) {
        final T managedEntity = persistenceContext.getEntity(entityType, id);
        if (Objects.nonNull(managedEntity)) {
            return managedEntity;
        }

        final T entity = entityLoader.load(entityType, id);
        persistenceContext.addEntity(entity);
        return entity;
    }

    @Override
    public void persist(Object entity) {
        validatePersist(entity);
        if (persistImmediately(entity)) {
            return;
        }

        persistenceContext.addEntity(entity);
        persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
        persistenceContext.addToPersistQueue(entity);
    }

    @Override
    public void persist(Object entity, Object parentEntity) {
        validatePersist(entity);
        if (persistImmediately(entity, parentEntity)) {
            return;
        }

        persistenceContext.addEntity(entity);
        persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
        persistenceContext.addToPersistQueue(entity);
    }

    @Override
    public void remove(Object entity) {
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entity);
        if (!entityEntry.isRemovable()) {
            throw new IllegalStateException(NOT_REMOVABLE_STATUS_FAILED_MESSAGE);
        }

        persistenceContext.removeEntity(entity);
        persistenceContext.addToRemoveQueue(entity);
    }

    @Override
    public void flush() {
        persistAll();
        deleteAll();
        updateAll();
    }

    @Override
    public void clear() {
        persistenceContext.clear();
    }

    private void validatePersist(Object entity) {
        final EntityEntry entityEntry = persistenceContext.getEntityEntry(entity);
        if (Objects.nonNull(entityEntry) && !entityEntry.isPersistable()) {
            throw new IllegalStateException(NOT_PERSISTABLE_STATUS_FAILED_MESSAGE);
        }
    }

    private boolean persistImmediately(Object entity) {
        final EntityTable entityTable = new EntityTable(entity);
        if (entityTable.isIdGenerationFromDatabase()) {
            entityPersister.insert(entity);
            persistenceContext.addEntity(entity);
            persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
            return true;
        }
        return false;
    }

    private boolean persistImmediately(Object entity, Object parentEntity) {
        final EntityTable entityTable = new EntityTable(entity);
        if (entityTable.isIdGenerationFromDatabase()) {
            entityPersister.insert(entity, parentEntity);
            persistenceContext.addEntity(entity);
            persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
            return true;
        }
        return false;
    }

    private void persistAll() {
        final Queue<Object> persistQueue = persistenceContext.getPersistQueue();
        while (!persistQueue.isEmpty()) {
            final Object entity = persistQueue.poll();
            entityPersister.insert(entity);
            persistenceContext.createOrUpdateStatus(entity, EntityStatus.MANAGED);
        }
    }

    private void deleteAll() {
        final Queue<Object> removeQueue = persistenceContext.getRemoveQueue();
        while (!removeQueue.isEmpty()) {
            final Object entity = removeQueue.poll();
            entityPersister.delete(entity);
        }
    }

    private void updateAll() {
        persistenceContext.getAllEntity()
                .forEach(this::update);
    }

    private void update(Object entity) {
        final EntityTable entityTable = new EntityTable(entity);
        final Object snapshot = persistenceContext.getSnapshot(entity.getClass(), entityTable.getIdValue());
        if (Objects.isNull(snapshot)) {
            return;
        }

        final List<EntityColumn> dirtiedEntityColumns = getDirtiedEntityColumns(entity, snapshot);
        if (dirtiedEntityColumns.isEmpty()) {
            return;
        }

        entityPersister.update(entity, dirtiedEntityColumns);
        persistenceContext.addEntity(entity);
    }

    private List<EntityColumn> getDirtiedEntityColumns(Object entity, Object snapshot) {
        final EntityTable entityTable = new EntityTable(entity);
        final EntityTable snapshotEntityTable = new EntityTable(snapshot);
        return IntStream.range(0, entityTable.getColumnCount())
                .filter(i -> isDirtied(entityTable.getEntityColumn(i), snapshotEntityTable.getEntityColumn(i)))
                .mapToObj(entityTable::getEntityColumn)
                .collect(Collectors.toList());
    }

    private boolean isDirtied(EntityColumn entityColumn, EntityColumn snapshotEntityColumn) {
        return !Objects.equals(entityColumn.getValue(), snapshotEntityColumn.getValue());
    }
}
