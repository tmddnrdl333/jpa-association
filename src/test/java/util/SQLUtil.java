package util;

public class SQLUtil {

    public static String SQL_노멀라이즈(String query) {
        return query.replaceAll("\n", "")
                .replaceAll("\\s{2,}", " ") // 공백 2개 이상을 1개로 줄임
                .replaceAll(", ", ",")
                .replaceAll("\\( ", "(");
    }
}
