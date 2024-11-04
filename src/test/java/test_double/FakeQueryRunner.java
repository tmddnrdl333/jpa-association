package test_double;

import jdbc.JdbcTemplate;
import jdbc.RowMapper;
import orm.dsl.QueryRunner;

import java.util.List;


public class FakeQueryRunner extends QueryRunner {

    public FakeQueryRunner() {
        super(new FakeJdbcTemplate());
    }
}

class FakeJdbcTemplate extends JdbcTemplate {
    public FakeJdbcTemplate() {
        super(null);
    }

    @Override
    public void execute(String sql) {
        // 아무 동작도 하지 않음
    }

    @Override
    public Object executeUpdateWithReturningGenKey(String sql) {
        throw new UnsupportedOperationException("FakeJdbcTemplate 에서는 사용할 수 없습니다.");
    }

    @Override
    public <T> List<T> query(String sql, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException("FakeJdbcTemplate 에서는 사용할 수 없습니다.");
    }

    @Override
    public <T> T queryForObject(String sql, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException("FakeJdbcTemplate 에서는 사용할 수 없습니다.");
    }
}
