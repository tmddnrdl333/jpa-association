package jdbc.mapping;

import jakarta.persistence.Transient;
import persistence.meta.EntityTable;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public interface FieldMapping {
    <T> boolean supports(Class<T> entityType);

    <T> T getRow(ResultSet resultSet, Class<T> entityType) throws SQLException, IllegalAccessException;

    default Class<?> getJoinColumnType(Class<?> entityType) {
        final List<Field> fields = getPersistentFields(entityType);
        final EntityTable entityTable = new EntityTable(entityType);
        return entityTable.getJoinColumnType();
    }

    default List<Field> getPersistentFields(Class<?> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Transient.class))
                .toList();
    }

    default void mapField(ResultSet resultSet, Object entity, Field field, int columnIndex) throws SQLException, IllegalAccessException {
        final Object value = resultSet.getObject(columnIndex);
        field.setAccessible(true);
        field.set(entity, value);
    }
}
