package steps;

import orm.dsl.QueryBuilder;
import orm.dsl.QueryRunner;
import persistence.sql.ddl.Person;

public class Steps {

    public static <T> void 테이블_생성(QueryRunner queryRunner, Class<T> entityClass) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.createTable(entityClass, queryRunner)
                .execute();
    }

    public static <T> void Person_엔티티_생성(QueryRunner queryRunner, Person person) {
        QueryBuilder queryBuilder = new QueryBuilder();
        queryBuilder.insertInto(Person.class, queryRunner)
                .value(person)
                .execute();

    }
}
