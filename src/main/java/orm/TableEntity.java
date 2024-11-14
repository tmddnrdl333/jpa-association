package orm;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import orm.assosiation.RelationFields;
import orm.dsl.holder.EntityIdHolder;
import orm.exception.EntityHasNoDefaultConstructorException;
import orm.exception.InvalidIdMappingException;
import orm.meta.EntityMeta;
import orm.settings.JpaSettings;
import orm.util.ReflectionUtils;
import orm.validator.EntityValidator;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 엔티티 클래스로부터 테이블 정보를 추출한 클래스
 *
 * @param <E> @Entity 어노테이션이 붙은 클래스
 */
public class TableEntity<E> {

    private static final Logger logger = LoggerFactory.getLogger(TableEntity.class);

    private final E entity;
    private final Class<?> tableClass;
    private final JpaSettings jpaSettings;

    // 엔티티 클래스 메타정보
    private final EntityMeta entityMeta;

    // 테이블의 ID 필드
    private final TablePrimaryField id;

    // 테이블의 모든 필드 (연관관계 제외)
    private final TableFields<E> tableFields;

    // 연관관계
    private final RelationFields<E> relationFields;

    private TableAlias alias;

    public TableEntity(Class<E> entityClass, JpaSettings settings) {
        this.entity = createNewInstanceByDefaultConstructor(entityClass);
        this.id = extractId(settings);
        this.entityMeta = new EntityMeta(this.entity, settings);
        this.tableFields = new TableFields<>(entity, settings);
        this.relationFields = new RelationFields<>(entity, settings);
        this.tableClass = entityClass;
        this.jpaSettings = settings;
    }

    public TableEntity(E entity, JpaSettings settings) {
        this.entity = entity;
        this.id = extractId(settings);
        this.entityMeta = new EntityMeta(entity, settings);
        this.tableFields = new TableFields<>(entity, settings);
        this.relationFields = new RelationFields<>(entity, settings);
        this.tableClass = entity.getClass();
        this.jpaSettings = settings;
    }

    public TableEntity(Class<E> entityClass) {
        this(entityClass, JpaSettings.ofDefault());
    }

    public TableEntity(E entity) {
        this(entity, JpaSettings.ofDefault());
    }

    public JpaSettings getJpaSettings() {
        return jpaSettings;
    }

    public String getTableName() {
        return entityMeta.getTableName();
    }

    public TablePrimaryField getId() {
        return id;
    }

    public void setIdValue(Object idValue) {
        id.setIdValue(idValue);
    }

    public void markFieldChanged(int index) {
        tableFields.setFieldChanged(index, true);
    }

    // DB에서 생성되는 ID인지 확인
    public boolean hasDbGeneratedId() {
        GenerationType idGenerationType = getIdGenerationType();
        return idGenerationType == GenerationType.IDENTITY;
    }

    // id(pk) 생성 전략
    public GenerationType getIdGenerationType() {
        GeneratedValue generatedValue = getId().getGeneratedValue();
        if (generatedValue == null) {
            return null;
        }
        return generatedValue.strategy();
    }

    // id 제외 모든 컬럼
    public List<TableField> getNonIdFields() {
        return tableFields.getNonIdFields();
    }

    // 변경된 컬럼만 리턴
    public List<TableField> getChangeFields() {
        return tableFields.getChangedFields();
    }

    // id를 포함 모든 컬럼
    public List<TableField> getAllFields() {
        return tableFields.getAllFields();
    }

    public Class<E> getTableClass() {
        return (Class<E>) tableClass;
    }

    public E getEntity() {
        return entity;
    }

    /**
     * 모든 필드를 주어진 필드로 교체한다.
     *
     * @param newTableFields 교체할 필드
     */
    public void replaceAllFields(List<? extends TableField> newTableFields) {
        tableFields.replaceAllFields(newTableFields);
    }

    /**
     * TableField에 세팅된 값들을 엔티티 클래스의 값에 적용한다.
     */
    public void syncFieldValueToEntity() {
        // non-id field들의 fieldName과 fieldValue를 매핑
        Map<String, Object> classFieldNameMap = this.getNonIdFields().stream()
                .filter(tableField -> tableField.getFieldValue() != null)
                .collect(Collectors.toMap(TableField::getClassFieldName, TableField::getFieldValue));

        // id field들의 fieldName과 fieldValue를 매핑
        classFieldNameMap.put(id.getClassFieldName(), id.getFieldValue());

        for (Field declaredField : tableClass.getDeclaredFields()) {
            var fieldValue = classFieldNameMap.get(declaredField.getName());
            ReflectionUtils.setFieldValue(declaredField, entity, fieldValue);
        }
    }

    public RelationFields<E> getRelationFields() {
        return this.relationFields;
    }

    public boolean hasRelationFields() {
        return relationFields.hasRelation();
    }

    private E createNewInstanceByDefaultConstructor(Class<E> entityClass) {
        try {
            Constructor<E> defaultConstructor = entityClass.getDeclaredConstructor();
            defaultConstructor.setAccessible(true);
            return defaultConstructor.newInstance();
        } catch (
                NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e
        ) {
            logger.error(e.getMessage());
            throw new EntityHasNoDefaultConstructorException("entity must contain default constructor");
        }
    }

    /**
     * 엔티티로부터 ID 추출
     *
     * @return TablePrimaryField ID 필드
     * @throws InvalidIdMappingException ID 필드가 없거나 2개 이상인 경우
     */
    private TablePrimaryField extractId(JpaSettings settings) {
        EntityIdHolder<E> entityIdHolder = new EntityIdHolder<>(entity);
        Field idField = entityIdHolder.getIdField();
        return new TablePrimaryField(idField, entity, settings);
    }

    public TableEntity<E> addAliasIfNotAssigned() {
        if(this.alias == null) {
            this.alias = new TableAlias(this.getTableName());
        }

        return this;
    }

    public TableEntity<E> addAliasIfNotAssigned(TableAlias alias) {
        if(this.alias == null) {
            this.alias = alias;
        }

        return this;
    }

    public boolean hasAlias() {
        return this.alias != null;
    }

    public TableAlias getAlias() {
        return alias;
    }

    public String getAliasName() {
        return alias.alias();
    }
}
