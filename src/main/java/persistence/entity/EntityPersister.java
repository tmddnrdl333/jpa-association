package persistence.entity;

import common.ReflectionFieldAccessUtils;
import jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.sql.definition.EntityTableMapper;
import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.dml.query.DeleteQueryBuilder;
import persistence.sql.dml.query.InsertQueryBuilder;
import persistence.sql.dml.query.UpdateQueryBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityPersister {
    private static final Long DEFAULT_ID_VALUE = 0L;
    private static final UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder();
    private static final InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder();
    private static final DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder();

    private final Logger logger = LoggerFactory.getLogger(EntityPersister.class);

    private final EntityTableMapper entityTableMapper;
    private final JdbcTemplate jdbcTemplate;

    public EntityPersister(Object entity, JdbcTemplate jdbcTemplate) {
        this.entityTableMapper = new EntityTableMapper(entity);
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean hasId() {
        return entityTableMapper.hasId();
    }

    public Serializable getEntityId() {
        if (hasId()) {
            return this.entityTableMapper.getIdValue();
        }

        return DEFAULT_ID_VALUE;
    }

    public Object insert(Object entity) {
        final String query = insertQueryBuilder.build(entity);
        final Serializable id = jdbcTemplate.insertAndReturnKey(query);

        bindId(id, entity);

        if (entityTableMapper.hasAssociation()) {
            final List<Object> persistedChildren = insertChildCollections(entity);
            persistedChildren.forEach(child -> updateAssociatedColumns(entity, child));
        }

        return entity;
    }

    private void updateAssociatedColumns(Object parent, Object child) {
        final EntityTableMapper parentMapper = new EntityTableMapper(parent);
        final EntityTableMapper childMapper = new EntityTableMapper(child);
        String updateQuery = updateQueryBuilder.build(parentMapper, childMapper);

        jdbcTemplate.execute(updateQuery);
    }

    private List<Object> insertChildCollections(Object parentEntity) {
        final EntityTableMapper entityTableMapper = new EntityTableMapper(parentEntity);
        final List<TableAssociationDefinition> associations = entityTableMapper.getAssociations();
        final List<Object> childEntities = new ArrayList<>();

        associations.forEach(association -> {
            final Collection<?> associatedValues = entityTableMapper.getIterableAssociatedValue(association);
            if (associatedValues instanceof Iterable<?> iterable) {
                iterable.forEach(entity -> {
                    Object result = insert(entity);
                    childEntities.add(result);
                });
            }
        });

        return childEntities;
    }

    public Collection<Object> getChildCollections(Object entity) {
        final EntityTableMapper entityTableMapper = new EntityTableMapper(entity);
        final List<TableAssociationDefinition> associations = entityTableMapper.getAssociations();
        final List<Object> childEntities = new ArrayList<>();

        associations.forEach(association -> {
            final Collection<?> associatedValues = entityTableMapper.getIterableAssociatedValue(association);
            if (associatedValues instanceof Iterable<?> iterable) {
                iterable.forEach(childEntities::add);
            }
        });

        return childEntities;
    }

    private void bindId(Serializable id, Object entity) {
        try {
            final EntityTableMapper entityTableMapper = new EntityTableMapper(entity);
            final Field idField = entityTableMapper.getEntityClass().getDeclaredField(entityTableMapper.getIdFieldName());

            ReflectionFieldAccessUtils.accessAndSet(entity, idField, id);
        } catch (ReflectiveOperationException e) {
            logger.error("Failed to copy row to {}", entity.getClass().getName(), e);
        }
    }

    public void update(Object entity) {
        final String query = updateQueryBuilder.build(entity);
        jdbcTemplate.execute(query);
    }

    public void delete(Object entity) {
        String query = deleteQueryBuilder.build(entity);
        jdbcTemplate.execute(query);
    }

}
