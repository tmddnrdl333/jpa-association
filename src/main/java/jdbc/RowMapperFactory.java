package jdbc;

import persistence.sql.definition.TableDefinition;

import java.util.HashMap;
import java.util.Map;

public class RowMapperFactory {
    private final Map<Class<?>, EagerFetchRowMapper<?>> eagerFetchRowMappers;
    private final Map<Class<?>, LazyFetchRowMapper<?>> lazyFetchRowMappers;

    private RowMapperFactory() {
        this.eagerFetchRowMappers = new HashMap<>();
        this.lazyFetchRowMappers = new HashMap<>();
    }

    private static class InstanceHolder {
        private static final RowMapperFactory INSTANCE = new RowMapperFactory();
    }

    public static RowMapperFactory getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public <T> RowMapper<T> createRowMapper(Class<T> targetClass, JdbcTemplate jdbcTemplate) {
        RowMapper<T> rowMapper = findCachedRowMapper(targetClass);
        if (rowMapper != null) {
            return rowMapper;
        }

        final TableDefinition tableDefinition = new TableDefinition(targetClass);
        for (var association : tableDefinition.getAssociations()) {
            if (association.isEager()) {
                eagerFetchRowMappers.put(targetClass, new EagerFetchRowMapper<>(targetClass));
                return new EagerFetchRowMapper<>(targetClass);
            }
        }

        lazyFetchRowMappers.put(targetClass, new LazyFetchRowMapper<>(targetClass, jdbcTemplate));
        return new LazyFetchRowMapper<>(targetClass, jdbcTemplate);
    }

    @SuppressWarnings("unchecked")
    private <T> RowMapper<T> findCachedRowMapper(Class<T> targetClass) {
        if (eagerFetchRowMappers.containsKey(targetClass)) {
            return (RowMapper<T>) eagerFetchRowMappers.get(targetClass);
        }

        if (lazyFetchRowMappers.containsKey(targetClass)) {
            return (RowMapper<T>) lazyFetchRowMappers.get(targetClass);
        }

        return null;
    }

}
