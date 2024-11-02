package persistence.entity;

import jdbc.DefaultRowMapper;
import jdbc.JdbcTemplate;
import org.jetbrains.annotations.Nullable;
import persistence.entity.proxy.ProxyFactory;
import persistence.meta.EntityColumn;
import persistence.meta.EntityTable;
import persistence.sql.dml.SelectQuery;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefaultEntityLoader implements EntityLoader {
    private final JdbcTemplate jdbcTemplate;
    private final SelectQuery selectQuery;
    private final ProxyFactory proxyFactory;

    public DefaultEntityLoader(JdbcTemplate jdbcTemplate, SelectQuery selectQuery, ProxyFactory proxyFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.selectQuery = selectQuery;
        this.proxyFactory = proxyFactory;
    }

    @Override
    public <T> T load(Class<T> entityType, Object id) {
        final String sql = selectQuery.findById(entityType, id);
        final T entity = jdbcTemplate.queryForObject(sql, new DefaultRowMapper<>(entityType));

        final Field associationField = getAssociationField(entityType);
        if (Objects.nonNull(associationField)) {
            setProxy(associationField, entity);
        }

        return entity;
    }

    @Override
    public <T> List<T> loadCollection(Class<T> entityType, String columnName, Object value) {
        final String sql = selectQuery.findCollection(entityType, columnName, value);
        return jdbcTemplate.query(sql, new DefaultRowMapper<>(entityType));
    }

    private <T> @Nullable Field getAssociationField(Class<T> entityType) {
        return Arrays.stream(entityType.getDeclaredFields())
                .filter(this::isOneToManyAndLazy)
                .findFirst()
                .orElse(null);
    }

    private boolean isOneToManyAndLazy(Field field) {
        final EntityColumn entityColumn = new EntityColumn(field);
        return entityColumn.isOneToManyAndLazy();
    }

    private void setProxy(Field associationField, Object entity) {
        final EntityTable entityTable = new EntityTable(entity);
        final List<Object> proxy = proxyFactory.createProxy(this, entityTable.getJoinColumnType(), entity);

        try {
            associationField.setAccessible(true);
            associationField.set(entity, proxy);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("프록시 생성에 실패하였습니다", e);
        }
    }
}
