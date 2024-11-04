package orm.row_mapper;

import jdbc.RowMapper;
import orm.TableEntity;
import orm.TableField;
import orm.exception.RowMapperException;

import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DefaultRowMapper<T> implements RowMapper<T> {

    private final Class<T> type;
    private final TableEntity<T> tableEntity;

    public DefaultRowMapper(T entity) {
        this.tableEntity = new TableEntity<>(entity);
        this.type = tableEntity.getTableClass();
    }

    public DefaultRowMapper(TableEntity<T> tableEntity) {
        this.tableEntity = tableEntity;
        this.type = tableEntity.getTableClass();
    }

    public T mapRow(ResultSet rs) throws RowMapperException {
        try {
            T instance = type.getDeclaredConstructor().newInstance();

            Map<String, TableField> classFieldMap = tableEntity.getAllFields().stream()
                    .collect(Collectors.toMap(TableField::getClassFieldName, Function.identity()));

            Field[] fields = type.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                TableField tableField = classFieldMap.get(field.getName());
                Object value = rs.getObject(tableField.getFieldName());

                field.set(instance, value);
            }

            return instance;
        } catch (Exception e) {
            throw new RowMapperException("Failed to map row to " + type.getName(), e);
        }
    }
}

