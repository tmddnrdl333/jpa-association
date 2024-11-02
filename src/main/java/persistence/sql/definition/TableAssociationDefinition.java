package persistence.sql.definition;

import common.ReflectionFieldAccessUtils;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TableAssociationDefinition {
    private final TableDefinition associatedTableDefinition;
    private final JoinColumn joinColumn;
    private final FetchType fetchType;
    private final String fieldName;

    public TableAssociationDefinition(Field field) {
        this.joinColumn = field.getAnnotation(JoinColumn.class);
        this.associatedTableDefinition = new TableDefinition(getGenericActualType(field));
        this.fieldName = field.getName();
        this.fetchType = getFetchType(field);
    }

    private static FetchType getFetchType(Field field) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            return field.getAnnotation(OneToMany.class).fetch();
        }

        if (field.isAnnotationPresent(ManyToMany.class)) {
            return field.getAnnotation(ManyToMany.class).fetch();
        }

        return FetchType.EAGER;
    }

    @NotNull
    private static Class<?> getGenericActualType(Field field) {
        final Type genericType = field.getGenericType();
        final Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

        return (Class<?>) actualTypeArguments[0];
    }

    public TableDefinition getAssociatedTableDefinition() {
        return associatedTableDefinition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getTableName() {
        return associatedTableDefinition.getTableName();
    }

    public List<? extends ColumnDefinitionAware> getColumns() {
        return associatedTableDefinition.getColumns();
    }

    public boolean isEager() {
        return fetchType == FetchType.EAGER;
    }

    public boolean isLazy() {
        return fetchType == FetchType.LAZY;
    }

    public String getJoinColumnName() {
        if (joinColumn != null) {
            return joinColumn.name();
        }
        return "";
    }

    public Collection<Object> getCollectionField(Object instance) throws NoSuchFieldException {
        final Field field = instance.getClass().getDeclaredField(getFieldName());
        Collection<Object> entityCollection = (Collection<Object>) ReflectionFieldAccessUtils.accessAndGet(instance, field);
        if (entityCollection == null) {
            entityCollection = new ArrayList<>();
            ReflectionFieldAccessUtils.accessAndSet(instance, field, entityCollection);
        }

        return entityCollection;
    }

    public void setCollectionField(Object instance, List collection) throws NoSuchFieldException {
        final Field field = instance.getClass().getDeclaredField(getFieldName());
        ReflectionFieldAccessUtils.accessAndSet(instance, field, collection);
    }

    public Class<?> getEntityClass() {
        return associatedTableDefinition.getEntityClass();
    }
}
