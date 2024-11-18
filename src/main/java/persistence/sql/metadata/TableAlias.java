package persistence.sql.metadata;

public record TableAlias(String value) {

    public TableAlias(TableName tableName) {
        this(tableName.value());
    }

    public TableAlias(Class<?> clazz) {
        this(alias(clazz));
    }

    private static String alias(Class<?> clazz) {
        String className = clazz.getSimpleName();

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < className.length(); i++) {
            char c = className.charAt(i);
            if (Character.isUpperCase(c)) {
                builder.append(Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

}
