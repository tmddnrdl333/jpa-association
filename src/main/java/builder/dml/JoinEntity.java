package builder.dml;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JoinEntity {

    private static final String GET_FIELD_VALUE_ERROR_MESSAGE = "필드 값을 가져오는 중 에러가 발생했습니다.";
    private final List<JoinEntityData> joinEntityData = new ArrayList<>();
    private JoinStatus joinStatus;
    private FetchType fetchType;

    public <T> JoinEntity(T entityInstance, Object joinColumnValue) {
        this.joinStatus = JoinStatus.FALSE;
        getInstanceColumnData(entityInstance, joinColumnValue);
    }

    public JoinEntity(Class<?> clazz, Object joinColumnValue) {
        this.joinStatus = JoinStatus.FALSE;
        getEntityColumnData(clazz, joinColumnValue);
    }

    public List<JoinEntityData> getJoinEntityData() {
        return joinEntityData;
    }

    public boolean checkJoin() {
        return this.joinStatus.isTrue();
    }

    public boolean checkFetchEager() {
        return this.fetchType == FetchType.EAGER;
    }

    public JoinEntityData findJoinEntityData(Class<?> clazz) {
        return this.joinEntityData.stream()
                .filter(entityData -> entityData.getClazz() == clazz)
                .findFirst()
                .orElse(null);
    }

    private void changeFetchType(Field field) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        this.fetchType = oneToMany.fetch();
    }

    private <T> void getInstanceColumnData(T entityInstance, Object joinColumnValue) {
        Arrays.stream(entityInstance.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .forEach(field -> createDMLInstanceColumnData(field, entityInstance, joinColumnValue));
    }

    private void getEntityColumnData(Class<?> entityClass, Object joinColumnName) {
        Arrays.stream(entityClass.getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(OneToMany.class))
                .forEach(field -> createDMLEntityColumnData(field, joinColumnName));
    }

    private <T> void createDMLInstanceColumnData(Field field, T entityInstance, Object joinColumnValue) {
        List<?> relatedEntities = getRelatedEntities(field, entityInstance);
        processRelatedEntities(field, relatedEntities, joinColumnValue);
    }

    private void createDMLEntityColumnData(Field field, Object joinColumnValue) {
        changeFetchType(field);

        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);

        Class<?> relatedEntityClass = getRelatedEntityClass(field);
        this.joinEntityData.add(new JoinEntityData(relatedEntityClass, joinColumn.name(), joinColumnValue));
        joinStatus = JoinStatus.TRUE;
    }

    private <T> List<?> getRelatedEntities(Field field, T entityInstance) {
        try {
            field.setAccessible(true);
            changeFetchType(field);

            return (List<?>) field.get(entityInstance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(GET_FIELD_VALUE_ERROR_MESSAGE + field.getName(), e);
        }
    }

    private void processRelatedEntities(Field field, List<?> relatedEntities, Object joinColumnValue) {
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        relatedEntities.forEach(object -> addJoinEntityData(object, joinColumn, joinColumnValue));
    }

    private void addJoinEntityData(Object object, JoinColumn joinColumn, Object joinColumnValue) {
        this.joinEntityData.add(new JoinEntityData(object, joinColumn.name(), joinColumnValue));
        joinStatus = JoinStatus.TRUE;
    }

    private Class<?> getRelatedEntityClass(Field field) {
        Type type = field.getGenericType();
        Type[] types = ((ParameterizedType) type).getActualTypeArguments();
        return (Class<?>) types[0];
    }

}
