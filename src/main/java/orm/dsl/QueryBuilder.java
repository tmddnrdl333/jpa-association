package orm.dsl;

import orm.SQLDialect;
import orm.TableEntity;
import orm.dsl.step.ddl.CreateTableStep;
import orm.dsl.step.ddl.DropTableStep;
import orm.dsl.step.dml.*;
import orm.settings.JpaSettings;

public class QueryBuilder implements QueryProvider {

    private final JpaSettings settings;

    public QueryBuilder() {
        this(JpaSettings.ofDefault());
    }

    public QueryBuilder(JpaSettings jpaSettings) {
        this.settings = jpaSettings;
    }

    public <E> CreateTableStep createTable(Class<E> entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .createTable(new TableEntity<>(entityClass, settings));
    }

    public <E> DropTableStep dropTable(Class<E> entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .dropTable(new TableEntity<>(entityClass, settings));
    }

    public <E> SelectFromStep<E> selectFrom(Class<E> entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .selectFrom(new TableEntity<>(entityClass, settings));
    }

    public <E> InsertIntoStep<E> insertInto(Class<E> entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .insert(new TableEntity<>(entityClass, settings));
    }

    public <E> ReturningStep<E> insertIntoValues(E entity, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .insert(new TableEntity<>(entity, settings))
                .value(entity);
    }

    public <E> DeleteFromStep deleteFrom(Class<E> entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .deleteFrom(new TableEntity<>(entityClass, settings));
    }

    public <E> DeleteFromStep deleteFrom(E entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .deleteFrom(new TableEntity<>(entityClass, settings));
    }

    public <E> UpdateStep<E> update(E entityClass, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .update(new TableEntity<>(entityClass, settings));
    }

    public <E> UpdateStep<E> update(TableEntity<E> entityEntity, QueryRunner queryRunner) {
        return new DialectStatementLocator(dialect(), queryRunner)
                .update(entityEntity);
    }

    public SQLDialect dialect() {
        return this.settings.getDialect();
    }
}
