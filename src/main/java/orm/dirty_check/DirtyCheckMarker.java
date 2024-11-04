package orm.dirty_check;

import orm.TableEntity;
import orm.TableField;
import orm.exception.DirtyCheckNotAllowedException;

import java.util.List;

public class DirtyCheckMarker<E> {

    private final TableEntity<E> entity;
    private final TableEntity<E> snapshotEntity;

    public DirtyCheckMarker(TableEntity<E> entity, TableEntity<E> snapshotEntity) {
        throwIfNotSameEntity(entity, snapshotEntity);
        throwIfIdNotSame(entity, snapshotEntity);
        this.entity = entity;
        this.snapshotEntity = snapshotEntity;
    }

    public DirtyCheckMarker(E entity, E snapshotEntity) {
        this(new TableEntity<>(entity), new TableEntity<>(snapshotEntity));
    }

    public TableEntity<E> getEntity() {
        return entity;
    }

    /**
     * 스냅샷과 비교하여 변경된 필드를 마킹한다.
     * @return 변경된 필드가 있는지 여부
     */
    public boolean compareAndMarkChangedField() {
        List<TableField> fields = entity.getAllFields();
        List<TableField> snapshotFields = snapshotEntity.getAllFields();

        boolean hasDirty = false;
        int fieldLength = fields.size();

        for (int fieldIdx = 0; fieldIdx < fieldLength; fieldIdx++) {
            TableField field = fields.get(fieldIdx);
            TableField snapshotField = snapshotFields.get(fieldIdx);

            if (!field.getFieldValue().equals(snapshotField.getFieldValue())) {
                entity.markFieldChanged(fieldIdx);
                hasDirty = true;
            }
        }

        return hasDirty;
    }

    private void throwIfNotSameEntity(TableEntity<E> cachedEntity, TableEntity<E> snapshotEntity) {
        if (!cachedEntity.getTableName().equals(snapshotEntity.getTableName())) {
            throw new DirtyCheckNotAllowedException("비교할 Entity가 서로 다른 테이블의 엔티티입니다.");
        }
    }

    private void throwIfIdNotSame(TableEntity<E> entity, TableEntity<E> snapshotEntity) {
        if (!entity.getId().getFieldValue().equals(snapshotEntity.getId().getFieldValue())) {
            throw new DirtyCheckNotAllowedException("비교할 Entity의 ID가 다릅니다.");
        }
    }
}
