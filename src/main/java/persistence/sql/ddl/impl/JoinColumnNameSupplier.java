package persistence.sql.ddl.impl;

import jakarta.persistence.JoinColumn;
import persistence.sql.common.util.NameConverter;
import persistence.sql.ddl.JoinQuerySupplier;

import java.lang.reflect.Field;

public class JoinColumnNameSupplier implements JoinQuerySupplier {
    private final short priority;
    private final NameConverter nameConverter;

    public JoinColumnNameSupplier(short priority, NameConverter nameConverter) {
        this.priority = priority;
        this.nameConverter = nameConverter;
    }

    @Override
    public short priority() {
        return priority;
    }

    @Override
    public boolean supported(JoinTargetDefinition definition) {
        return definition != null;
    }

    @Override
    public String supply(JoinTargetDefinition definition) {
        Field joinedField = definition.getJoinedField();

        if (joinedField == null) {
            throw new IllegalArgumentException("Joined field must not be null");
        }

        JoinColumn anno = joinedField.getAnnotation(JoinColumn.class);
        if (anno != null && !anno.name().isBlank()) {
            return nameConverter.convert(anno.name());
        }

        return nameConverter.convert(joinedField.getName());
    }
}
