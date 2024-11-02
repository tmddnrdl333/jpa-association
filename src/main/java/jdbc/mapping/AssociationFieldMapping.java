package jdbc.mapping;

import jdbc.InstanceFactory;
import persistence.meta.EntityTable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class AssociationFieldMapping implements FieldMapping {
    @Override
    public <T> boolean supports(Class<T> entityType) {
        final EntityTable entityTable = new EntityTable(entityType);
        return !entityTable.isSimpleMapping();
    }

    @Override
    public <T> T getRow(ResultSet resultSet, Class<T> entityType) throws IllegalAccessException, SQLException {
        final List<Field> fields = getPersistentFields(entityType);
        final Class<?> joinColumnType = getJoinColumnType(entityType);

        final T entity = new InstanceFactory<>(entityType).createInstance();
        final List<Object> list = getList(fields, entity);

        do {
            list.add(getChildEntity(resultSet, joinColumnType, fields, entity));
        } while (resultSet.next());

        return entity;
    }

    private List<Object> getList(List<Field> fields, Object entity) throws IllegalAccessException {
        final Field listField = findListField(fields);
        listField.setAccessible(true);
        return (List<Object>) listField.get(entity);
    }

    private Object getChildEntity(ResultSet resultSet, Class<?> joinColumnType, List<Field> fields, Object entity) throws SQLException, IllegalAccessException {
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

    private Field findListField(List<Field> fields) {
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
}
