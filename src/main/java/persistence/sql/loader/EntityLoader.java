package persistence.sql.loader;

import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import persistence.proxy.ProxyFactory;
import persistence.sql.QueryBuilderFactory;
import persistence.sql.clause.Clause;
import persistence.sql.clause.LeftJoinClause;
import persistence.sql.clause.WhereConditionalClause;
import persistence.sql.common.util.CamelToSnakeConverter;
import persistence.sql.common.util.NameConverter;
import persistence.sql.context.CollectionKeyHolder;
import persistence.sql.context.PersistenceContext;
import persistence.sql.data.QueryType;
import persistence.sql.dml.Database;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.dml.impl.SimpleMetadataLoader;
import persistence.sql.entity.CollectionEntry;
import persistence.sql.entity.data.Status;
import persistence.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class EntityLoader<T> implements Loader<T> {
    private static final Logger logger = Logger.getLogger(EntityLoader.class.getName());

    private final Database database;
    private final MetadataLoader<T> metadataLoader;
    private final NameConverter nameConverter;
    private final ProxyFactory proxyFactory;

    public EntityLoader(Class<T> entityType, Database database, ProxyFactory proxyFactory) {
        this(database,
                new SimpleMetadataLoader<>(entityType),
                CamelToSnakeConverter.getInstance(), proxyFactory);
    }

    public EntityLoader(Database database,
                        MetadataLoader<T> metadataLoader,
                        NameConverter nameConverter, ProxyFactory proxyFactory) {
        this.database = database;
        this.metadataLoader = metadataLoader;
        this.nameConverter = nameConverter;
        this.proxyFactory = proxyFactory;
    }

    private static boolean isEager(Field field) {
        OneToMany anno = field.getAnnotation(OneToMany.class);

        return anno != null && anno.fetch() == FetchType.EAGER;
    }

    private static boolean isLazy(Field field) {
        OneToMany anno = field.getAnnotation(OneToMany.class);

        return anno != null && anno.fetch() == FetchType.LAZY;
    }

    public MetadataLoader<T> getMetadataLoader() {
        return metadataLoader;
    }

    @Override
    public List<T> loadAll() {
        String selectQuery = QueryBuilderFactory.getInstance().buildQuery(QueryType.SELECT, metadataLoader);

        return database.executeQuery(selectQuery, resultSet -> {
            List<T> entities = new ArrayList<>();

            while (resultSet.next()) {
                entities.add(mapRow(resultSet));
            }

            return entities;
        });
    }

    @Override
    public List<T> loadAllByForeignKey(Object foreignKey, MetadataLoader<?> foreignLoader) {
        String selectQuery = createSelectQuery(foreignKey, foreignLoader);

        return database.executeQuery(selectQuery, resultSet -> {
            List<T> entities = new ArrayList<>();

            while (resultSet.next()) {
                entities.add(mapRow(resultSet));
            }

            return entities;
        });
    }

    @Override
    public T load(Object primaryKey) {
        String selectQuery = createSelectQuery(primaryKey);

        return database.executeQuery(selectQuery, resultSet -> {
            if (resultSet.next()) {
                return mapRow(resultSet);
            }

            return null;
        });
    }

    @Override
    public T loadByForeignKey(Object foreignKey, MetadataLoader<?> foreignLoader) {
        String selectQuery = createSelectQuery(foreignKey, foreignLoader);

        return database.executeQuery(selectQuery, resultSet -> {
            if (resultSet.next()) {
                return mapRow(resultSet);
            }

            return null;
        });

    }

    private String createSelectQuery(Object foreignKey, MetadataLoader<?> foreignLoader) {
        List<Clause> clauses = new ArrayList<>();
        String value = Clause.toColumnValue(foreignKey);

        WhereConditionalClause clause = WhereConditionalClause.builder(metadataLoader.getTableAlias())
                .column(extractForeignColumnName(foreignLoader))
                .eq(value);
        clauses.add(clause);

        if (joinable()) {
            clauses.addAll(createJoinQuery());
        }

        return QueryBuilderFactory.getInstance().buildQuery(QueryType.SELECT, metadataLoader, clauses.toArray(Clause[]::new));
    }

    private String extractForeignColumnName(MetadataLoader<?> foreignLoader) {
        List<Field> foreignFields = foreignLoader.getFieldAllByPredicate(field -> {
            boolean hasAnno = field.isAnnotationPresent(OneToMany.class);
            Class<?> type = field.getType();
            if (Collection.class.isAssignableFrom(field.getType())) {
                type = ReflectionUtils.collectionClass(field.getGenericType());
            }

            return hasAnno && type.equals(metadataLoader.getEntityType());
        });

        if (foreignFields.isEmpty()) {
            throw new IllegalArgumentException("No foreign key found");
        }

        if (foreignFields.size() > 1) {
            throw new IllegalArgumentException("Multiple foreign keys found");
        }

        return foreignLoader.getJoinColumnName(foreignFields.getFirst(), nameConverter);
    }

    private String createSelectQuery(Object primaryKey) {

        List<Clause> clauses = new ArrayList<>();
        String value = Clause.toColumnValue(primaryKey);

        WhereConditionalClause clause = WhereConditionalClause.builder(metadataLoader.getTableAlias())
                .column(metadataLoader.getColumnName(metadataLoader.getPrimaryKeyField(), nameConverter))
                .eq(value);
        clauses.add(clause);

        if (joinable()) {
            clauses.addAll(createJoinQuery());
        }

        return QueryBuilderFactory.getInstance().buildQuery(QueryType.SELECT, metadataLoader, clauses.toArray(Clause[]::new));
    }

    private boolean joinable() {
        return !metadataLoader.getFieldAllByPredicate(EntityLoader::isEager).isEmpty();
    }

    private List<? extends Clause> createJoinQuery() {
        List<Clause> clauses = new ArrayList<>();
        List<Field> joinFields = metadataLoader.getFieldAllByPredicate(EntityLoader::isEager);

        for (Field joinField : joinFields) {
            Type genericType = joinField.getGenericType();
            Class<?> joinType = ReflectionUtils.collectionClass(genericType);
            LeftJoinClause leftJoinClause = LeftJoinClause.of(metadataLoader.getEntityType(), joinType);
            clauses.add(leftJoinClause);
        }

        return clauses;
    }

    public T mapRow(ResultSet resultSet) {
        try {
            T entity = metadataLoader.getNoArgConstructor().newInstance();

            int columnCount = metadataLoader.getFieldAllByPredicate(field -> !isLazy(field)).size();
            AtomicInteger cur = new AtomicInteger(1);

            while (cur.get() <= columnCount) {
                Field curField = metadataLoader.getField(cur.get() - 1);

                Object columnValue = getColumnValue(resultSet, curField, cur);
                cur.incrementAndGet();
                curField.setAccessible(true);
                curField.set(entity, columnValue);
            }

            return entity;
        } catch (ReflectiveOperationException | SQLException e) {
            logger.severe("Failed to map row to entity");
            throw new IllegalStateException(e);
        }
    }

    private Object getColumnValue(ResultSet resultSet, Field curField, AtomicInteger cur) throws SQLException {
        if (isAssociationEagerField(curField)) {
            return getAssociationField(resultSet, curField, cur);
        }

        return resultSet.getObject(cur.get());
    }

    private boolean isAssociationEagerField(Field curField) {
        OneToMany anno = curField.getAnnotation(OneToMany.class);
        return Collection.class.isAssignableFrom(curField.getType()) && anno != null && anno.fetch() == FetchType.EAGER;
    }

    private Object getAssociationField(ResultSet resultSet, Field curField, AtomicInteger cur) {
        try {
            Collection<Object> collection = new ArrayList<>();
            Type genericType = curField.getGenericType();
            Class<?> joinType = ReflectionUtils.collectionClass(genericType);
            MetadataLoader<?> joinLoader = new SimpleMetadataLoader<>(joinType);

            do {
                Object joinEntity = joinLoader.getNoArgConstructor().newInstance();

                int columnCount = joinLoader.getColumnCount();
                for (int i = 0; i < columnCount; i++) {
                    Field joinField = joinLoader.getField(i);
                    Object columnValue = resultSet.getObject(cur.get() + i);
                    joinField.setAccessible(true);
                    joinField.set(joinEntity, columnValue);
                }

                collection.add(joinEntity);
            } while (resultSet.next());

            return collection;
        } catch (ReflectiveOperationException | SQLException e) {
            logger.severe("Failed to handle association field");
            throw new IllegalStateException(e);
        }
    }

    public boolean existLazyLoading() {
        return !metadataLoader.getFieldAllByPredicate(EntityLoader::isLazy).isEmpty();
    }

    public void updateLazyLoadingField(T parentEntity,
                                       PersistenceContext persistenceContext,
                                       BiConsumer<CollectionKeyHolder, CollectionEntry> onAfterLoadConsumer) {
        List<Field> lazyFields = metadataLoader.getFieldAllByPredicate(EntityLoader::isLazy);
        Object foreignKey = Clause.extractValue(metadataLoader.getPrimaryKeyField(), parentEntity);

        for (Field lazyField : lazyFields) {
            Class<? extends Collection<Object>> lazyFieldType = ReflectionUtils.getCollectionFieldType(lazyField);
            Class<?> lazyFieldGenericType = ReflectionUtils.collectionClass(lazyField.getGenericType());
            MetadataLoader<?> lazyLoader = new SimpleMetadataLoader<>(lazyFieldGenericType);

            Collection<Object> lazyProxy = proxyFactory.createProxyCollection(foreignKey,
                    metadataLoader.getEntityType(),
                    lazyFieldGenericType,
                    lazyFieldType,
                    persistenceContext);

            try {
                lazyField.setAccessible(true);
                lazyField.set(parentEntity, lazyProxy);
            } catch (IllegalAccessException e) {
                logger.severe("Failed to update lazy loading field");
                throw new IllegalStateException(e);
            }

            CollectionEntry collectionEntry = CollectionEntry.create(lazyLoader, Status.MANAGED, lazyProxy);
            CollectionKeyHolder collectionKeyHolder = new CollectionKeyHolder(parentEntity.getClass(), foreignKey, lazyFieldGenericType);

            onAfterLoadConsumer.accept(collectionKeyHolder, collectionEntry);
        }
    }
}
