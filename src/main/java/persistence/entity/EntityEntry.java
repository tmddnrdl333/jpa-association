package persistence.entity;

public class EntityEntry {
    private EntityEntryStatus status;

    public EntityEntry(EntityEntryStatus status) {
        this.status = status;
    }

    public EntityEntryStatus getStatus() {
        return status;
    }

    public void setStatus(EntityEntryStatus newStatus) {
        if (!status.isTransitiveTo(newStatus)) {
            throw new IllegalStateException("INVALID STATUS TRANSITION FROM " + status + " TO " + newStatus);
        }
        status = newStatus;
    }

    public boolean isImmutable() {
        return this.status != EntityEntryStatus.MANAGED;
    }

    public boolean isUndeletable() {
        return this.status != EntityEntryStatus.MANAGED;
    }
}
