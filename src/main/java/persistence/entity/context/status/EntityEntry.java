package persistence.entity.context.status;

import persistence.entity.context.EntityKey;

public class EntityEntry {

    EntityKey key;
    EntityStatus status;

    public EntityEntry(EntityKey key, EntityStatus status) {
        this.key = key;
        this.status = status;
    }

    public void updateStatus(EntityStatus status) {
        this.status = status;
    }

}
