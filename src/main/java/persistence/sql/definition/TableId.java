package persistence.sql.definition;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import persistence.sql.SqlType;
import persistence.sql.ddl.query.AutoKeyGenerationStrategy;
import persistence.sql.ddl.query.IdentityKeyGenerationStrategy;
import persistence.sql.ddl.query.PrimaryKeyGenerationStrategy;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class TableId implements ColumnDefinitionAware {
    private static final List<PrimaryKeyGenerationStrategy> pkGenerationStrategies = List.of(
            new AutoKeyGenerationStrategy(),
            new IdentityKeyGenerationStrategy()
    );

    private final GenerationType generationType;
    private final ColumnDefinition columnDefinition;
    private final PrimaryKeyGenerationStrategy strategy;

    public TableId(Class<?> entityClass) {
        final Field[] fields = entityClass.getDeclaredFields();
        final Field pkField = Arrays.stream(fields)
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Primary key not found"));

        this.columnDefinition = new ColumnDefinition(pkField);
        this.generationType = determineGenerationType(pkField);
        this.strategy = findProperGenerationStrategy();
    }

    private static GenerationType determineGenerationType(Field field) {
        final boolean hasGeneratedValueAnnotation = field.isAnnotationPresent(GeneratedValue.class);

        if (hasGeneratedValueAnnotation) {
            GeneratedValue generatedValue = field.getAnnotation(GeneratedValue.class);
            return generatedValue.strategy();
        }

        return GenerationType.AUTO;
    }

    private PrimaryKeyGenerationStrategy findProperGenerationStrategy() {
        return pkGenerationStrategies.stream()
                .filter(pkStrategy -> pkStrategy.supports(this))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported primary key generation strategy"));
    }

    public String generatePrimaryKeySQL() {
        return strategy.generatePrimaryKeySQL();
    }

    public GenerationType generationType() {
        return generationType;
    }

    @Override
    public String getDatabaseColumnName() {
        return columnDefinition.getColumnName();
    }

    @Override
    public String getEntityFieldName() {
        return columnDefinition.getDeclaredName();
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public int getLength() {
        return columnDefinition.getLength();
    }

    @Override
    public SqlType getSqlType() {
        return columnDefinition.getSqlType();
    }

    @Override
    public boolean isPrimaryKey() {
        return true;
    }
}
