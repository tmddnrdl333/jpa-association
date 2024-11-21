package persistence.entity.persister;

import static persistence.sql.dml.query.WhereOperator.EQUAL;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import jdbc.JdbcTemplate;
import persistence.entity.EntityIdExtractor;
import persistence.meta.ColumnMeta;
import persistence.meta.ColumnValueMeta;
import persistence.meta.RelationMeta;
import persistence.meta.SchemaMeta;
import persistence.meta.TableMeta;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.dml.query.builder.DeleteQueryBuilder;
import persistence.sql.dml.query.builder.InsertQueryBuilder;
import persistence.sql.dml.query.builder.UpdateQueryBuilder;

public class DefaultEntityPersister implements EntityPersister {

    private final JdbcTemplate jdbcTemplate;

    public DefaultEntityPersister(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public <T> Object insert(T entity) {
        insertEntity(entity);
        insertRelatedEntity(entity);
        return entity;
    }

    private <T> void insertEntity(T entity) {
        TableMeta tableMeta = new TableMeta(entity.getClass());
        List<ColumnMeta> columnMetas = Arrays.stream(entity.getClass().getDeclaredFields())
                .map(ColumnMeta::new)
                .filter(ColumnMeta::isNotPrimaryKey)
                .filter(ColumnMeta::hasNotRelation)
                .toList();
        List<Object> columnValues = columnMetas.stream()
                .map(columnMeta -> ColumnValueMeta.of(columnMeta.field(), entity))
                .map(ColumnValueMeta::value)
                .toList();

        String query = InsertQueryBuilder.builder()
                .insert(tableMeta, columnMetas)
                .values(columnValues)
                .build();
        Object parentId = jdbcTemplate.insertAndGetPrimaryKey(query);
        updateEntityId(entity, parentId);
    }

    private <T> void insertRelatedEntity(T entity) {
        List<ColumnMeta> columnMetas = Arrays.stream(entity.getClass().getDeclaredFields())
                .map(ColumnMeta::new)
                .filter(ColumnMeta::isNotPrimaryKey)
                .filter(ColumnMeta::hasRelation)
                .toList();

        for (ColumnMeta columnMeta : columnMetas) {
            RelationMeta relationMeta = columnMeta.relationMeta();
            List<?> relatedEntities = extractEntities(entity, columnMeta);
            for (Object relatedEntity : relatedEntities) {
                SchemaMeta schemaMeta = new SchemaMeta(relatedEntity);
                String query = InsertQueryBuilder.builder()
                        .insert(relationMeta.joinTableName(), schemaMeta.columnNamesWithoutPrimaryKey(), List.of(relationMeta.joinColumnName()))
                        .values(schemaMeta.columnValuesWithoutPrimaryKey(), List.of(EntityIdExtractor.extractIdValue(entity)))
                        .build();

                Object id = jdbcTemplate.insertAndGetPrimaryKey(query);
                updateEntityId(relatedEntity, id);
            }
        }
    }


    private <T> List<?> extractEntities(T entity, ColumnMeta columnMeta) {
        try {
            Field field = entity.getClass().getDeclaredField(columnMeta.field().getName());
            field.setAccessible(true);
            return (List<?>) field.get(entity);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void updateEntityId(T entity, Object id) {
        Field idField = EntityIdExtractor.extractIdField(entity);
        idField.setAccessible(true);
        try {
            idField.set(entity, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> void update(T entity) {
        SchemaMeta schemaMeta = new SchemaMeta(entity);
        String query = UpdateQueryBuilder.builder()
                .update(schemaMeta.tableName())
                .set(schemaMeta.columnNamesWithoutPrimaryKey(), schemaMeta.columnValuesWithoutPrimaryKey())
                .where(List.of(new WhereCondition(schemaMeta.primaryKeyColumnName(), EQUAL, EntityIdExtractor.extractIdValue(entity))))
                .build();
        jdbcTemplate.execute(query);
    }

    @Override
    public <T> void delete(T entity) {
        SchemaMeta schemaMeta = new SchemaMeta(entity);
        String query = DeleteQueryBuilder.builder()
                .delete(schemaMeta.tableName())
                .build();
        jdbcTemplate.execute(query);
    }

}
