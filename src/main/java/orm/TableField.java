package orm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.exception.CannotExtractEntityFieldValueException;
import orm.meta.EntityFieldMeta;
import orm.settings.JpaSettings;

import java.lang.reflect.Field;

public class TableField {

    private static final Logger log = LoggerFactory.getLogger(TableField.class);
    private final Field field;

    private final EntityFieldMeta entityFieldMeta;
    private Object fieldValue;

    public <T> TableField(Field field, T entity, JpaSettings jpaSettings) {
        this.field = field;
        this.entityFieldMeta = new EntityFieldMeta(field, jpaSettings);
        this.fieldValue = extractFieldValue(field, entity);
    }

    public String getFieldName() {
        return entityFieldMeta.getFieldName();
    }

    public String getClassFieldName() {
        return entityFieldMeta.getClassFieldName();
    }

    public ColumnMeta getColumnMeta() {
        return entityFieldMeta.getColumnMeta();
    }

    public Class<?> getFieldType() {
        return field.getType();
    }

    public Object getFieldValue() {
        return fieldValue;
    }

    protected void setFieldValue(Object newFieldValue) {
        this.fieldValue = newFieldValue;
    }

    public boolean isId() {
        return false;
    }

    private <T> Object extractFieldValue(Field field, T entity) {
        try {
            field.setAccessible(true);
            return field.get(entity);
        } catch (IllegalAccessException e) {
            log.error("Cannot access field: " + field.getName(), e);
            throw new CannotExtractEntityFieldValueException("Cannot extract field value: " + field.getName(), e);
        }
    }
}
