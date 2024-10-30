package jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JdbcTemplate {
    private static final Logger logger = LoggerFactory.getLogger(JdbcTemplate.class);

    private static final String QUERY_EXECUTE_FAILED_MESSAGE = "쿼리 실행을 실패하였습니다.";

    private final Connection connection;

    public JdbcTemplate(final Connection connection) {
        this.connection = connection;
    }

    public void execute(final String sql) {
        logger.debug(sql);
        try (final PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.execute();
        } catch (Exception e) {
            throw new IllegalArgumentException(QUERY_EXECUTE_FAILED_MESSAGE);
        }
    }

    public void executeAndReturnGeneratedKeys(final String sql, final IdMapper idMapper) {
        logger.debug(sql);
        try (final PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.execute();
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                idMapper.mapRow(generatedKeys);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(QUERY_EXECUTE_FAILED_MESSAGE);
        }
    }

    public <T> T queryForObject(final String sql, final RowMapper<T> rowMapper) {
        final List<T> results = query(sql, rowMapper);
        if (results.size() != 1) {
            throw new IllegalStateException("Expected 1 result, got " + results.size());
        }
        return results.get(0);
    }

    public <T> List<T> query(final String sql, final RowMapper<T> rowMapper) {
        logger.debug(sql);
        try (final ResultSet resultSet = connection.prepareStatement(sql).executeQuery()) {
            final List<T> result = new ArrayList<>();
            while (resultSet.next()) {
                result.add(rowMapper.mapRow(resultSet));
            }
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException(QUERY_EXECUTE_FAILED_MESSAGE);
        }
    }
}
