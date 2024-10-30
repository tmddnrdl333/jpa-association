package common;

public class SqlLogger {
    private SqlLogger() {
    }

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(SqlLogger.class);

    public static void infoCreateTable(String query) {
        logger.info("Creating table with query: {}", query);
    }

    public static void infoInsert(String query) {
        logger.info("Inserting with query: {}", query);
    }

    public static void infoUpdate(String query) {
        logger.info("Updating with query: {}", query);
    }

    public static void infoSelect(String query) {
        logger.info("Selecting with query: {}", query);
    }
}
