package persistence.sql.ddl.impl;

import java.lang.reflect.Field;
import java.util.Objects;

public class JoinTargetDefinition {
    private final Class<?> joinedEntity;
    private final Field joinedField;
    private final Class<?> targetEntity;

    public JoinTargetDefinition(Class<?> joinedEntity, Field joinedField, Class<?> targetEntity) {
        this.joinedEntity = joinedEntity;
        this.joinedField = joinedField;
        this.targetEntity = targetEntity;
    }

    public Class<?> getJoinedEntity() {
        return joinedEntity;
    }

    public Field getJoinedField() {
        return joinedField;
    }

    public Class<?> getTargetEntity() {
        return targetEntity;
    }

    @Override
    public String toString() {
        return "JoinTargetDefinition{" +
                "joinedEntity=" + joinedEntity +
                ", joinedField=" + joinedField +
                ", targetEntity=" + targetEntity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        JoinTargetDefinition that = (JoinTargetDefinition) o;
        return Objects.equals(joinedEntity, that.joinedEntity) && Objects.equals(joinedField, that.joinedField) && Objects.equals(targetEntity, that.targetEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(joinedEntity, joinedField, targetEntity);
    }
}
