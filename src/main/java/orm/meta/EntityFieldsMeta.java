package orm.meta;

import orm.settings.JpaSettings;

import java.util.Arrays;
import java.util.List;

public class EntityFieldsMeta {

    private final Class<?> entityClass;
    private final List<EntityFieldMeta> entityFieldMetaList;
    private final JpaSettings settings;

    public EntityFieldsMeta(Class<?> entityClass, JpaSettings settings) {
        this.settings = settings;
        this.entityClass = entityClass;
        this.entityFieldMetaList = initEntityFieldMetaList();
    }

    public EntityFieldsMeta(Class<?> entityClass) {
        this(entityClass, JpaSettings.ofDefault());
    }

    private List<EntityFieldMeta> initEntityFieldMetaList() {
        return Arrays.stream(entityClass.getDeclaredFields())
                .map(field -> new EntityFieldMeta(field, settings))
                .toList();
    }

    public List<EntityFieldMeta> getNonRelationFields() {
        return entityFieldMetaList.stream()
                .filter(meta -> !meta.isRelationAnnotation())
                .toList();
    }

    public List<EntityFieldMeta> getOneToManyRelationFields() {
        return entityFieldMetaList.stream()
                .filter(EntityFieldMeta::isOneToManyAssociated)
                .toList();
    }
}
