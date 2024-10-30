package persistence.sql.ddl.query;

import persistence.sql.definition.TableId;

public interface PrimaryKeyGenerationStrategy {
    String generatePrimaryKeySQL();

    boolean supports(TableId pk);
}
