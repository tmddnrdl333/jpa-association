package persistence.sql.context;

import java.io.Serializable;
import java.util.Objects;

public record CollectionKeyHolder(Class<?> parentType, Object parentKey, Class<?> childType) implements Serializable {
    public CollectionKeyHolder {
        if (parentType == null || parentKey == null || childType == null) {
            throw new IllegalArgumentException("entityType and key must not be null");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CollectionKeyHolder that)) {
            return false;
        }
        return Objects.equals(parentKey, that.parentKey)
                && Objects.equals(childType, that.childType)
                && Objects.equals(parentType, that.parentType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentType, parentKey, childType);
    }
}
