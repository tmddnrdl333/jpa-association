package sample.persistence.sql;

import static persistence.sql.dml.query.WhereOperator.EQUAL;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import persistence.exception.NotExistException;
import persistence.exception.UnknownException;
import persistence.meta.SchemaMeta;
import persistence.sql.dml.query.WhereCondition;
import persistence.sql.dml.query.builder.SelectQueryBuilder;

public class AssociationSelectQueryBuilder {

    public List<String> selectQueries(Class<?> clazz, Object id) {
        Optional<Field> oneToManyField = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findAny();
        OneToMany oneToMany = oneToManyField.get().getAnnotation(OneToMany.class);
        FetchType fetchType = oneToMany.fetch();

        List<String> queries = new ArrayList<>();
        if (FetchType.EAGER == fetchType) {
            SchemaMeta parentSchemaMeta = new SchemaMeta(clazz);
            String parentQuery = SelectQueryBuilder.builder()
                    .select()
                    .from(parentSchemaMeta.tableName())
                    .where(List.of(new WhereCondition(parentSchemaMeta.primaryKeyColumnName(), EQUAL, id)))
                    .build();
            queries.add(parentQuery);

            SchemaMeta childSchemaMeta = new SchemaMeta(childClass(oneToManyField.get()));
            String childSelectQueryString = SelectQueryBuilder.builder()
                    .select()
                    .from(childSchemaMeta.tableName())
                    .where(List.of(new WhereCondition(joinColumnName(oneToManyField.get()), EQUAL, id)))
                    .build();
            queries.add(childSelectQueryString);
        }
        return queries;
    }


    private String joinColumnName(Field field) {
        if (hasNotJoinColumnAnnotation(field)) {
            throw new NotExistException("@JoinColumn annotation. field: " + field.getName());
        }
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        return joinColumn.name();
    }

    private boolean hasJoinColumnAnnotation(Field field) {
        return field.isAnnotationPresent(JoinColumn.class);
    }

    private boolean hasNotJoinColumnAnnotation(Field field) {
        return !hasJoinColumnAnnotation(field);
    }

    private Class<?> childClass(Field field) {
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType parameterizedType) {
            Type childType = parameterizedType.getActualTypeArguments()[0];

            if (childType instanceof Class<?>) {
                return (Class<?>) childType;
            }
        }
        throw new UnknownException("Type " + genericType);
    }

}
