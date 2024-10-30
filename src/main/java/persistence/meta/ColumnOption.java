package persistence.meta;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

public class ColumnOption {
    private final boolean isNotNull;
    private final boolean isOneToManyAssociation;
    private final FetchType fetchType;
    private final String joinColumnName;
    private final Class<?> joinColumnType;

    public ColumnOption(Field field) {
        this.isNotNull = isNotNull(field);
        this.isOneToManyAssociation = isOneToManyAssociation(field);
        this.fetchType = getFetchType(field);
        this.joinColumnName = getJoinColumnName(field);
        this.joinColumnType = getJoinColumnType(field);
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public boolean isOneToManyAssociation() {
        return isOneToManyAssociation;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public String getJoinColumnName() {
        return joinColumnName;
    }

    public Class<?> getJoinColumnType() {
        return joinColumnType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnOption that = (ColumnOption) o;
        return isNotNull == that.isNotNull;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(isNotNull);
    }

    private boolean isNotNull(Field field) {
        final Column column = field.getAnnotation(Column.class);
        if (Objects.isNull(column)) {
            return false;
        }
        return !column.nullable();
    }

    private boolean isOneToManyAssociation(Field field) {
        final OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        return Objects.nonNull(oneToMany);
    }

    private FetchType getFetchType(Field field) {
        final OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        if (Objects.isNull(oneToMany)) {
            return null;
        }
        return oneToMany.fetch();
    }

    private String getJoinColumnName(Field field) {
        final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (Objects.isNull(joinColumn) || Objects.isNull(joinColumn.name()) || joinColumn.name().isBlank()) {
            return null;
        }
        return joinColumn.name();
    }

    private Class<?> getJoinColumnType(Field field) {
        final JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        if (Objects.isNull(joinColumn)) {
            return null;
        }
        return getGenericType(field);
    }

    private Class<?> getGenericType(Field field) {
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (typeArguments.length > 0 && typeArguments[0] instanceof Class<?>) {
                return (Class<?>) typeArguments[0];
            }
        }
        return null;
    }
}
