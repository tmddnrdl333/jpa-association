package database.mapping;

import database.dialect.Dialect;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class Association {

    private final Field field;

    public Association(Field field) {
        this.field = field;
    }

    public static Association fromField(Field field) {
        return new Association(field);
    }

    public String getForeignKeyColumnName() {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        return joinColumn.name();
    }

    public String getFieldName() {
        return field.getName();
    }

    public Class<?> getFieldType() {
        return field.getType();
    }

    public Class<?> getFieldGenericType() {
        return (Class<?>) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
    }

    public String getTableName() {
        return getOwnerEntityMetadata().getTableName();
    }

    private EntityMetadata getOwnerEntityMetadata() {
        return EntityMetadataFactory.get(this.getFieldGenericType());
    }

    public String getForeignKeyColumnType(Dialect dialect) {
        Type foreignKeyColumnType = getOwnerEntityMetadata().getPrimaryKey().getFieldType();

        return dialect.convertToSqlTypeDefinition((Class<?>) foreignKeyColumnType, 0);
    }

    public boolean isLazyLoad() {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        return oneToMany.fetch() == FetchType.LAZY;
    }
}
