package persistence.sql.dml.query;

import persistence.sql.definition.EntityTableMapper;

public class DeleteQueryBuilder {

    public String build(Object entity) {
        final StringBuilder query = new StringBuilder();
        final EntityTableMapper entityTableMapper = new EntityTableMapper(entity);

        query.append("DELETE FROM ");
        query.append(entityTableMapper.getTableName());

        query.append(" WHERE ");
        query.append(entityTableMapper.getIdColumnName())
                .append(" = ")
                .append(entityTableMapper.getIdValue()).append(";");
        return query.toString();
    }
}
