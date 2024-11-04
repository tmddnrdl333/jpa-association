package persistence.sql.ddl.impl;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import org.reflections.Reflections;
import persistence.sql.ddl.JoinTargetScanner;
import persistence.sql.node.EntityNode;
import persistence.sql.node.FieldNode;
import persistence.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 애노테이션 기반 연관관계 정보 스캐너
 */
public class AnnotatedJoinTargetScanner implements JoinTargetScanner {
    private Reflections reflections;

    @Override
    @SuppressWarnings("unchecked")
    public Set<JoinTargetDefinition> scan(String basePackage) {
        reflections = new Reflections(basePackage);

        return getTypesAnnotatedWith(Entity.class).stream()
                .filter(this::hasJoinTarget)
                .map(this::getJoinTargetDefinition)
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    private Set<JoinTargetDefinition> getJoinTargetDefinition(Class<?> joinedTarget) {
        return EntityNode.from(joinedTarget)
                .fields().stream()
                .filter(field -> field.containsAnnotations(OneToMany.class))
                .map(field -> new JoinTargetDefinition(joinedTarget, field.getField(), getTargetEntity(field)))
                .collect(Collectors.toSet());
    }

    private Class<?> getTargetEntity(FieldNode field) {
        Type genericType = field.getField().getGenericType();

        return ReflectionUtils.collectionClass(genericType);
    }

    private boolean hasJoinTarget(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredFields())
                .anyMatch(field -> field.isAnnotationPresent(OneToMany.class));
    }

    @SuppressWarnings("unchecked")
    private Set<Class<?>> getTypesAnnotatedWith(Class<? extends Annotation>... annotations) {
        Set<Class<?>> beans = new HashSet<>();
        for (Class<? extends Annotation> annotation : annotations) {
            beans.addAll(reflections.getTypesAnnotatedWith(annotation));
        }
        return beans;
    }
}
