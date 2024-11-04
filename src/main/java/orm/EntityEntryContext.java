package orm;

import orm.life_cycle.EntityEntry;
import orm.life_cycle.SimpleEntityEntry;
import orm.life_cycle.Status;

import java.util.HashMap;
import java.util.Map;

public class EntityEntryContext {

    protected final Map<EntityKey, EntityEntry> entryMap;

    public EntityEntryContext() {
        this.entryMap = new HashMap<>();
    }

    public EntityEntryContext(Map<EntityKey, EntityEntry> entryMap) {
        this.entryMap = entryMap;
    }

    public EntityEntry addEntry(EntityKey entityKey, Status status) {
        if (entityKey.hasNullIdValue()) {
            return null;
        }

        EntityEntry simpleEntityEntry = new SimpleEntityEntry(entityKey, status);
        entryMap.put(entityKey, simpleEntityEntry);
        return simpleEntityEntry;
    }

    public EntityEntry getEntry(EntityKey entityKey) {
        return entryMap.get(entityKey);
    }

    public void removeEntry(EntityKey entityKey) {
        entryMap.remove(entityKey);
    }
}
