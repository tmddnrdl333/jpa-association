package persistence.meta;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import persistence.exception.NotExistException;
import persistence.exception.UnknownException;

public record RelationMeta(Class<?> joinColumnType,
                           String joinTableName,
                           String joinColumnName,
                           FetchType fetchType,
                           boolean hasRelation) {

    private RelationMeta() {
        this(null, null, null, null, false);
    }

    private RelationMeta(Class<?> joinColumnType, String joinTableName, String joinColumnName, FetchType fetchType) {
        this(joinColumnType, joinTableName, joinColumnName, fetchType, true);
    }

    public static RelationMeta from(Field field) {
        if (field.isAnnotationPresent(OneToMany.class)) {
            return oneToManyRelation(field);
        }

        return new RelationMeta();
    }

    private static RelationMeta oneToManyRelation(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type type = parameterizedType.getActualTypeArguments()[0];
            Class<?> typeClazz = (Class<?>) type;
            OneToMany oneToMany = field.getAnnotation(OneToMany.class);
            return new RelationMeta(
                    typeClazz,
                    (new TableMeta(typeClazz)).name(),
                    joinColumnName(field),
                    oneToMany.fetch()
            );
        }
        throw new UnknownException("ParameterizedType type: " + genericType.getTypeName());
    }

    private static String joinColumnName(Field field) {
        if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
            return joinColumn.name();
        }
        throw new NotExistException("@JoinColumn annotation. field: " + field.getName());
    }

    public Class<?> getJoinColumnType() {
        return joinColumnType;
    }

    public String getJoinTableName() {
        return joinTableName;
    }

    public String getJoinColumnName() {
        return joinColumnName;
    }

    public FetchType getFetchType() {
        return fetchType;
    }

    public boolean hasRelation() {
        return hasRelation;
    }

    public boolean hasNotRelation() {
        return !hasRelation();
    }

}
