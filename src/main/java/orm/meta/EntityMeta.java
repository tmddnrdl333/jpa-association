package orm.meta;

import orm.settings.JpaSettings;
import orm.validator.EntityValidator;

public class EntityMeta {

    private final JpaSettings jpaSettings;
    private final Object entity;
    private final String tableName;

    public EntityMeta(Object entity, JpaSettings jpaSettings) {
        new EntityValidator<>(entity).validate();
        this.jpaSettings = jpaSettings;
        this.entity = entity;
        this.tableName = jpaSettings.getNamingStrategy().namingTable(entity.getClass());
    }

    public String getTableName() {
        return tableName;
    }
}
