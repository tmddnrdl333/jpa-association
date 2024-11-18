package persistence.sql.ddl.query.constraint;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import persistence.sql.ddl.query.association.Association;
import persistence.sql.ddl.query.association.AssociationType;
import persistence.util.PackageScanner;
import persistence.validator.AnnotationValidator;

public class ForeignKeyConstraintProvider {

    private static final ForeignKeyConstraintProvider INSTANCE = new ForeignKeyConstraintProvider();
    private static final String SCAN_PACKAGE_NAME = "sample";

    private static Map<Class<?>, ForeignKeyConstraint> foreignKeyConstraints;

    public static ForeignKeyConstraintProvider getInstance() {
        return INSTANCE;
    }

    private ForeignKeyConstraintProvider() {
        List<Class<?>> classes = scanEntityClass();

        foreignKeyConstraints = new HashMap<>();
        Arrays.stream(AssociationType.values())
                .forEach(type -> addConstraints(classes, type));
    }

    public static void addConstraints(List<Class<?>> classes, AssociationType associationType) {
        Map<Class<?>, Optional<Field>> associations = classes.stream()
                .collect(Collectors.toMap(
                        clazz -> clazz,
                        clazz -> Arrays.stream(clazz.getDeclaredFields())
                                .filter(field -> AnnotationValidator.isPresent(field, associationType.type()))
                                .findFirst()
                ));

        for (Class<?> clazz : associations.keySet()) {
            Optional<Field> associationField = associations.get(clazz);
            if (associationField.isEmpty()) {
                continue;
            }

            Association association = associationType.association();
            foreignKeyConstraints.put(clazz, association.foreignKeyConstraint(clazz, associationField.get()));
        }
    }

    private List<Class<?>> scanEntityClass() {
        try {
            return PackageScanner.scan(SCAN_PACKAGE_NAME);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
