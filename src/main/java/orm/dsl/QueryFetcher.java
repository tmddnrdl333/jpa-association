package orm.dsl;

import jdbc.RowMapper;

import java.util.List;

public interface QueryFetcher<E> {

    // rowMapper를 명시적으로 구현
    E fetchOne(RowMapper<E> rowMapper);

    List<E> fetch();

    E fetchOne();
}
