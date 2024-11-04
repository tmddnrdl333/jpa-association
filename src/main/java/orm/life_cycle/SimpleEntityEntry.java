package orm.life_cycle;

import orm.EntityKey;

public class SimpleEntityEntry implements EntityEntry {

    private final EntityKey entityKey;
    private Status status;

    public SimpleEntityEntry(EntityKey entityKey, Status status) {
        this.entityKey = entityKey;
        this.status = status;
    }

    @Override
    public Status getStatus() {
        return status;
    }

    @Override
    public EntityEntry updateStatus(Status status) {
        this.status = status;
        return this;
    }

    @Override
    public boolean isStatus(Status status) {
        return this.status == status;
    }

    @Override
    public Object getId() {
        return entityKey.idValue();
    }
}
