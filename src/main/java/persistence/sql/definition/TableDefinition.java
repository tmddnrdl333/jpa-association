package persistence.sql.definition;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class TableDefinition {

    private final Class<?> entityClass;
    private final String tableName;
    private final TableId tableId;
    private final List<? extends ColumnDefinitionAware> columns;
    private final List<TableAssociationDefinition> associations;

    public TableDefinition(Class<?> entityClass) {
        validateEntityAnnotationPresent(entityClass);
        validateHasOneId(entityClass);

        this.entityClass = entityClass;
        this.tableName = getDatabaseTableName(entityClass);
        this.tableId = new TableId(entityClass);
        this.associations = createAssociations(entityClass);
        this.columns = createTableColumns(entityClass);
    }

    private static List<TableAssociationDefinition> createAssociations(Class<?> entityClass) {
        final List<Field> collectionFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(TableDefinition::isAssociationAnnotationPresent)
                .filter(field -> Collection.class.isAssignableFrom(field.getType()))
                .toList();

        if (collectionFields.isEmpty()) {
            return List.of();
        }

        return collectionFields.stream()
                .map(TableAssociationDefinition::new)
                .toList();
    }

    private static boolean isAssociationAnnotationPresent(Field field) {
        return field.isAnnotationPresent(OneToMany.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(jakarta.persistence.OneToOne.class)
                || field.isAnnotationPresent(jakarta.persistence.ManyToOne.class);
    }


    @NotNull
    private static String getDatabaseTableName(Class<?> entityClass) {
        final String tableName = entityClass.getSimpleName();

        if (entityClass.isAnnotationPresent(Table.class)) {
            Table tableAnnotation = entityClass.getAnnotation(Table.class);
            if (!tableAnnotation.name().isEmpty()) {
                return tableAnnotation.name();
            }
        }

        if (entityClass.isAnnotationPresent(Entity.class)) {
            Entity entityAnnotation = entityClass.getAnnotation(Entity.class);
            if (!entityAnnotation.name().isEmpty()) {
                return entityAnnotation.name();
            }
        }

        return tableName;
    }

    private static List<? extends ColumnDefinitionAware> createTableColumns(Class<?> entityClass) {
        return Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> !field.isAnnotationPresent(Transient.class))
                .filter(field -> !Collection.class.isAssignableFrom(field.getType()))
                .map(TableColumn::new)
                .toList();
    }

    private void validateEntityAnnotationPresent(Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Entity must be annotated with @Entity");
        }
    }

    private void validateHasOneId(Class<?> entityClass) {
        List<Field> idFields = Arrays.stream(entityClass.getDeclaredFields())
                .filter(field ->
                        field.isAnnotationPresent(Id.class)
                ).toList();

        if (idFields.size() != 1) {
            throw new IllegalArgumentException("Entity must have exactly one field annotated with @Id");
        }
    }

    public TableId getTableId() {
        return tableId;
    }

    public String getTableName() {
        return tableName;
    }

    public List<? extends ColumnDefinitionAware> getColumns() {
        return columns;
    }

    public List<TableAssociationDefinition> getAssociations() {
        return associations;
    }

    public boolean hasAssociations() {
        return !associations.isEmpty();
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getIdColumnName() {
        return tableId.getDatabaseColumnName();
    }

    public TableAssociationDefinition getAssociation(Class<?> associatedEntityClass) {
        for (TableAssociationDefinition association : getAssociations()) {
            if (association.getEntityClass().equals(associatedEntityClass)) {
                return association;
            }
        }

        return null;
    }

    public String getJoinColumnName(Class<?> associatedEntityClass) {
        final TableAssociationDefinition association = getAssociation(associatedEntityClass);
        return association.getJoinColumnName();
    }

    public String getIdFieldName() {
        return tableId.getEntityFieldName();
    }
}
