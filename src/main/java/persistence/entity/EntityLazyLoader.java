package persistence.entity;

import java.util.Collection;

public interface EntityLazyLoader {
    Collection<?> loadLazyCollection(Object owner);
}
