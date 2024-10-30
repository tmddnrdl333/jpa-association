package jdbc;

import jakarta.persistence.Transient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import persistence.meta.EntityTable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("rawtypes")
public class DefaultRowMapper<T> implements RowMapper<T> {
    private static final Logger logger = LoggerFactory.getLogger(DefaultRowMapper.class);

    private final Class<T> entityType;

    public DefaultRowMapper(Class<T> entityType) {
        this.entityType = entityType;
    }

    @Override
    public T mapRow(ResultSet resultSet) throws SQLException, IllegalAccessException {
        final List<Field> fields = getPersistentFields(entityType);
        final EntityTable entityTable = new EntityTable(entityType);
        final Class<?> joinColumnType = entityTable.getJoinColumnType();

        if (hasChildren(joinColumnType)) {
            return getEntityWithChildren(resultSet, joinColumnType, fields);
        }
        return getEntityWithOutChildren(resultSet, fields);
    }

    private List<Field> getPersistentFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Transient.class))
                .toList();
    }

    private boolean hasChildren(Class<?> joinColumnType) {
        return joinColumnType != Object.class;
    }

    @SuppressWarnings("unchecked")
    private T getEntityWithChildren(ResultSet resultSet, Class<?> joinColumnType, List<Field> fields) throws SQLException, IllegalAccessException {
        final T entity = new InstanceFactory<>(entityType).createInstance();
        final List list = getList(fields, entity);

        do {
            list.add(getChildEntity(resultSet, joinColumnType, fields, entity));
        } while (resultSet.next());

        return entity;
    }

    private List getList(List<Field> fields, T entity) throws IllegalAccessException {
        final Field listField = getListField(fields);
        listField.setAccessible(true);
        return (List) listField.get(entity);
    }

    private Object getChildEntity(ResultSet resultSet, Class<?> joinColumnType, List<Field> fields, T entity) throws SQLException, IllegalAccessException {
        final Object childEntity = new InstanceFactory<>(joinColumnType).createInstance();
        final AtomicInteger fieldIndex = new AtomicInteger(0);
        final AtomicInteger childFieldIndex = new AtomicInteger(0);

        for (int i = 0; i < getColumnCount(resultSet); i++) {
            Field field = getField(fields, fieldIndex);
            if (Objects.nonNull(field)) {
                mapField(resultSet, entity, field, i + 1);
                continue;
            }

            Field childField = getField(getPersistentFields(joinColumnType), childFieldIndex);
            mapField(resultSet, childEntity, childField, i + 1);
        }
        return childEntity;
    }

    private int getColumnCount(ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        return metaData.getColumnCount();
    }

    private Field getListField(List<Field> fields) {
        return fields.stream()
                .filter(field -> field.getType() == List.class)
                .findFirst()
                .orElseThrow();
    }

    private Field getField(List<Field> fields, AtomicInteger fieldIndex) {
        while (fieldIndex.get() < fields.size()) {
            final Field field = fields.get(fieldIndex.getAndAdd(1));

            if (field.getType() != List.class) {
                return field;
            }
        }

        return null;
    }

    private T getEntityWithOutChildren(ResultSet resultSet, List<Field> fields) throws SQLException, IllegalAccessException {
        final T entity = new InstanceFactory<>(entityType).createInstance();
        for (int i = 0; i < fields.size(); i++) {
            mapField(resultSet, entity, fields.get(i), i + 1);
        }
        return entity;
    }

    private void mapField(ResultSet resultSet, Object entity, Field field, int columnIndex) throws SQLException, IllegalAccessException {
        final Object value = resultSet.getObject(columnIndex);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
