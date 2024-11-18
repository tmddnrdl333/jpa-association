package persistence.sql.dml.query;

import static persistence.validator.AnnotationValidator.isNotPresent;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import persistence.sql.metadata.TableName;


public record InsertQuery(TableName tableName,
                          List<ColumnNameValue> columns) {
    public InsertQuery(Object clazzObject) {
        this(
                new TableName(clazzObject.getClass()),
                Arrays.stream(clazzObject.getClass().getDeclaredFields())
                        .filter(field -> isNotPresent(field, Transient.class))
                        .filter(field -> isNotPresent(field, Id.class))
                        .map(field -> new ColumnNameValue(clazzObject, field))
                        .toList()
        );
    }

}
