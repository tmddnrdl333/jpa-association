package persistence.sql.dml.query.builder;

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
import persistence.sql.dml.query.SelectQuery;
import persistence.sql.dml.query.WhereCondition;

public class AssociationSelectQueryBuilder {

    public List<String> selectQueries(Class<?> clazz, Object id) {
        Optional<Field> oneToManyField = Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .findAny();
        OneToMany oneToMany = oneToManyField.get().getAnnotation(OneToMany.class);
        FetchType fetchType = oneToMany.fetch();

        List<String> queries = new ArrayList<>();
        if (FetchType.EAGER == fetchType) {
            SelectQuery parentSelectQuery = new SelectQuery(clazz);
            String parentSelectQueryString = SelectQueryBuilder.builder()
                    .select(parentSelectQuery.columnNames())
                    .from(parentSelectQuery.tableName())
                    .where(List.of(new WhereCondition("id", "=", id)))
                    .build();
            queries.add(parentSelectQueryString);

            SelectQuery childSelectQuery = new SelectQuery(childClass(oneToManyField.get()));
            String childSelectQueryString = SelectQueryBuilder.builder()
                    .select(childSelectQuery.columnNames())
                    .from(childSelectQuery.tableName())
                    .where(List.of(new WhereCondition(joinColumnName(oneToManyField.get()), "=", id)))
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
