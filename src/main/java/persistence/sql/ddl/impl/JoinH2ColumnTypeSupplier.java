package persistence.sql.ddl.impl;

import persistence.sql.common.util.NameConverter;
import persistence.sql.ddl.Dialect;
import persistence.sql.ddl.JoinQuerySupplier;
import persistence.sql.node.EntityNode;
import persistence.sql.node.FieldNode;

import java.sql.Types;
import java.util.Map;

public class JoinH2ColumnTypeSupplier implements JoinQuerySupplier {
    private static final Map<Class<?>, Integer> columnTypeMap = Map.of(
            Integer.class, Types.INTEGER,
            Long.class, Types.BIGINT,
            String.class, Types.VARCHAR
    );

    private final short priority;
    private final Dialect dialect;

    public JoinH2ColumnTypeSupplier(short priority, Dialect dialect) {
        this.priority = priority;
        this.dialect = dialect;
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
        Class<?> joinedEntity = definition.getJoinedEntity();

        if (joinedEntity == null) {
            throw new IllegalArgumentException("Joined field must not be null");
        }

        EntityNode<?> entityNode = EntityNode.from(joinedEntity);
        FieldNode idField = entityNode.getIdField();

        Class<?> fieldType = idField.getFieldType();
        Integer columnTypeIndex = columnTypeMap.get(fieldType);
        String columnType = dialect.get(columnTypeIndex);

        if (columnType == null) {
            throw new IllegalArgumentException("Unsupported field type: " + fieldType);
        }

        if (Types.VARCHAR == columnTypeIndex) {
            // TODO: Add support for custom length
            return columnType + "(255)";
        }

        return columnType;
    }
}
