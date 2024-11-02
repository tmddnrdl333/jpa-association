package persistence.entity;

import common.ReflectionFieldAccessUtils;
import jdbc.JdbcTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.sql.definition.TableAssociationDefinition;
import persistence.sql.definition.TableDefinition;
import persistence.sql.dml.query.DeleteQueryBuilder;
import persistence.sql.dml.query.InsertQueryBuilder;
import persistence.sql.dml.query.UpdateQueryBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EntityPersister {
    private static final Long DEFAULT_ID_VALUE = 0L;
    private static final UpdateQueryBuilder updateQueryBuilder = new UpdateQueryBuilder();
    private static final InsertQueryBuilder insertQueryBuilder = new InsertQueryBuilder();
    private static final DeleteQueryBuilder deleteQueryBuilder = new DeleteQueryBuilder();

    private final Logger logger = LoggerFactory.getLogger(EntityPersister.class);
    private final Map<Class<?>, TableDefinition> tableDefinitions;

    private final JdbcTemplate jdbcTemplate;

    public EntityPersister(JdbcTemplate jdbcTemplate) {
        this.tableDefinitions = new HashMap<>();
        this.jdbcTemplate = jdbcTemplate;
    }

    private TableDefinition getTableDefinition(Class<?> entityClass) {
        return tableDefinitions.computeIfAbsent(entityClass, TableDefinition::new);
    }

    public boolean hasId(Object entity) {
        return getTableDefinition(entity.getClass()).hasId(entity);
    }

    public Serializable getEntityId(Object entity) {
        final TableDefinition tableDefinition = getTableDefinition(entity.getClass());
        if (tableDefinition.hasId(entity)) {
            return tableDefinition.getIdValue(entity);
        }

        return DEFAULT_ID_VALUE;
    }

    public Object insert(Object entity) {
        final TableDefinition tableDefinition = getTableDefinition(entity.getClass());
        final String query = insertQueryBuilder.build(entity);
        final Serializable id = jdbcTemplate.insertAndReturnKey(query);

        bindId(id, entity);

        if (tableDefinition.hasAssociations()) {
            final List<Object> persistedChildren = insertChildCollections(entity);
            persistedChildren.forEach(child -> updateAssociatedColumns(entity, child));
        }

        return entity;
    }

    private void updateAssociatedColumns(Object parent, Object child) {
        final TableDefinition parentDefinition = getTableDefinition(parent.getClass());
        final TableDefinition childDefinition = getTableDefinition(child.getClass());
        String updateQuery = updateQueryBuilder.build(parent, child, parentDefinition, childDefinition);

        jdbcTemplate.execute(updateQuery);
    }

    private List<Object> insertChildCollections(Object parentEntity) {
        final TableDefinition parentTableDefinition = getTableDefinition(parentEntity.getClass());
        final List<TableAssociationDefinition> associations = parentTableDefinition.getAssociations();
        final List<Object> childEntities = new ArrayList<>();

        associations.forEach(association -> {
            final Collection<?> associatedValues = parentTableDefinition.getIterableAssociatedValue(parentEntity, association);
            if (associatedValues instanceof Iterable<?> iterable) {
                iterable.forEach(entity -> {
                    Object result = insert(entity);
                    childEntities.add(result);
                });
            }
        });

        return childEntities;
    }

    public Collection<Object> getChildCollections(Object childEntity) {
        final TableDefinition childTableDefinition = getTableDefinition(childEntity.getClass());
        final List<TableAssociationDefinition> associations = childTableDefinition.getAssociations();
        final List<Object> childEntities = new ArrayList<>();

        associations.forEach(association -> {
            final Collection<?> associatedValues = childTableDefinition.getIterableAssociatedValue(childEntity, association);
            if (associatedValues instanceof Iterable<?> iterable) {
                iterable.forEach(childEntities::add);
            }
        });

        return childEntities;
    }

    private void bindId(Serializable id, Object entity) {
        try {
            final TableDefinition tableDefinition = getTableDefinition(entity.getClass());
            final Field idField = tableDefinition.getEntityClass().getDeclaredField(tableDefinition.getIdFieldName());

            ReflectionFieldAccessUtils.accessAndSet(entity, idField, id);
        } catch (ReflectiveOperationException e) {
            logger.error("Failed to copy row to {}", entity.getClass().getName(), e);
        }
    }

    public void update(Object entity) {
        final String query = updateQueryBuilder.build(entity, getTableDefinition(entity.getClass()));
        jdbcTemplate.execute(query);
    }

    public void delete(Object entity) {
        String query = deleteQueryBuilder.build(entity);
        jdbcTemplate.execute(query);
    }

}
