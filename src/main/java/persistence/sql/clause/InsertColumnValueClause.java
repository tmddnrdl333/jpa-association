package persistence.sql.clause;

import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import persistence.sql.common.util.NameConverter;
import persistence.sql.data.ClauseType;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.dml.impl.SimpleMetadataLoader;
import persistence.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public record InsertColumnValueClause(String column, String value) implements ValueClause {

    public static InsertColumnValueClause newInstance(Object entity, NameConverter nameConverter) {
        MetadataLoader<?> loader = new SimpleMetadataLoader<>(entity.getClass());

        List<Field> fields = loader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(OneToMany.class));
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (Field field : fields) {
            columns.add(loader.getColumnName(field, nameConverter));
            values.add(Clause.toColumnValue(Clause.extractValue(field, entity)));
        }

        return new InsertColumnValueClause(String.join(", ", columns), String.join(", ", values));
    }

    public static InsertColumnValueClause newInstance(Object entity, Object parentEntity, NameConverter nameConverter) {
        MetadataLoader<?> loader = new SimpleMetadataLoader<>(entity.getClass());
        MetadataLoader<?> parentLoader = new SimpleMetadataLoader<>(parentEntity.getClass());

        List<Field> fields = loader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(Id.class) && !field.isAnnotationPresent(OneToMany.class));
        List<String> columns = new ArrayList<>();
        List<String> values = new ArrayList<>();

        for (Field field : fields) {
            columns.add(loader.getColumnName(field, nameConverter));
            values.add(Clause.toColumnValue(Clause.extractValue(field, entity)));
        }

        List<Field> targetFields = parentLoader.getFieldAllByPredicate(field -> Collection.class.isAssignableFrom(field.getType())
                && ReflectionUtils.collectionClass(field.getGenericType()).equals(entity.getClass()));

        for (Field field : targetFields) {
            columns.add(parentLoader.getJoinColumnName(field, nameConverter));
            values.add(Clause.toColumnValue(Clause.extractValue(parentLoader.getPrimaryKeyField(), parentEntity)));
        }

        return new InsertColumnValueClause(String.join(", ", columns), String.join(", ", values));
    }

    @Override
    public boolean supported(ClauseType clauseType) {
        return clauseType == ClauseType.INSERT;
    }

    @Override
    public String clause() {
        return "(" + value() + ")";
    }
}
