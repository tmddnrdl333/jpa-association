package persistence.sql.entity;

import jakarta.persistence.Id;
import persistence.sql.clause.Clause;
import persistence.sql.dml.MetadataLoader;
import persistence.sql.entity.data.Status;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CollectionEntry {
    private final MetadataLoader<?> genericTypeLoader;
    private boolean loaded;
    private Status status;
    private Collection<Object> entries;
    private Collection<Object> snapshotEntries;

    private CollectionEntry(MetadataLoader<?> loader, boolean loaded, Status status, Collection<Object> entries, Collection<Object> snapshotEntries) {
        this.genericTypeLoader = loader;
        this.loaded = loaded;
        this.status = status;
        this.entries = entries;
        this.snapshotEntries = snapshotEntries;
    }

    public static CollectionEntry create(MetadataLoader<?> loader, Status status, Collection<Object> entries) {
        return new CollectionEntry(loader, false, status, entries, null);
    }

    @SuppressWarnings("unchecked")
    private static <T> Collection<T> createSnapshot(Collection<T> entries, MetadataLoader<?> loader) {
        try {
            List<T> snapshotEntries = new ArrayList<>(entries.size());
            for (T entry : entries) {
                T snapshot = (T) loader.getNoArgConstructor().newInstance();
                overwritingObject(loader, entry, snapshot);
                snapshotEntries.add(snapshot);
            }

            return snapshotEntries;
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Failed to create snapshot entity");
        }
    }

    private static <T> void overwritingObject(MetadataLoader<?> loader, T entry, T snapshot) throws IllegalAccessException {
        for (Field field : loader.getFieldAllByPredicate(field -> true)) {
            field.setAccessible(true);
            field.set(snapshot, field.get(entry));
        }
    }

    public <T> List<T> getEntries() {
        return (List<T>) entries;
    }

    public void updateStatus(Status status) {
        this.status = status;
    }

    public void add(Object entity) {
        if (!genericTypeLoader.getEntityType().isInstance(entity)) {
            throw new IllegalArgumentException("Invalid entity type");
        }

        this.entries.add(entity);
    }

    public void synchronizingSnapshot() {
        if (snapshotEntries == null) {
            snapshotEntries = createSnapshot(entries, genericTypeLoader);
            return;
        }

        List<Object> entryArrayList = new ArrayList<>(entries);
        List<Object> snapshotArrayList = new ArrayList<>(snapshotEntries);

        for (int i = 0; i < entries.size(); i++) {
            Object entity = entryArrayList.get(i);
            Object snapshot = snapshotArrayList.get(i);

            genericTypeLoader.getFieldAllByPredicate(field -> !field.isAnnotationPresent(Id.class))
                    .forEach(field -> copyFieldValue(field, entity, snapshot));
        }
    }

    private void copyFieldValue(Field field, Object entity, Object origin) {
        try {
            field.setAccessible(true);
            Object value = field.get(entity);
            field.set(origin, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Illegal access to field: " + field.getName());
        }
    }

    public boolean isDirty() {
        if (isNotManagedStatus()) {
            return false;
        }

        if (!(snapshotEntries == null && entries == null) && snapshotEntries == null || entries == null) {
            return true;
        }
        List<Object> entryArrayList = new ArrayList<>(entries);
        List<Object> snapshotArrayList = new ArrayList<>(snapshotEntries);

        for (int i = 0; i < entries.size(); i++) {
            Object entity = entryArrayList.get(i);
            Object snapshot = snapshotArrayList.get(i);

            if (isDirty(entity, snapshot)) {
                return true;
            }
        }

        return false;
    }

    public boolean isDirty(Object entity, Object snapshot) {
        if (!(snapshot == null && entity == null) && snapshot == null || entity == null) {
            return true;
        }

        List<Field> fields = genericTypeLoader.getFieldAllByPredicate(field -> {
            Object entityValue = Clause.extractValue(field, entity);
            Object snapshotValue = Clause.extractValue(field, snapshot);

            if (entityValue == null && snapshotValue == null) {
                return false;
            }

            if (entityValue == null || snapshotValue == null) {
                return true;
            }

            return !entityValue.equals(snapshotValue);
        });

        return !fields.isEmpty();
    }


    private boolean isNotManagedStatus() {
        return !Status.isManaged(status);
    }

    public <T> List<T> getSnapshotEntries() {
        return new ArrayList<>((Collection<? extends T>) snapshotEntries);
    }

    public Status getStatus() {
        return status;
    }

    public boolean isInitialize() {
        return loaded;
    }

    public void updateEntries(List<Object> target) {
        this.entries = target;
        this.snapshotEntries = createSnapshot(target, genericTypeLoader);
        this.loaded = true;
    }
}
