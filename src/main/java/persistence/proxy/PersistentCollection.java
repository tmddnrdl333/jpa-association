package persistence.proxy;

public interface PersistentCollection extends LazyInitializer {
    Object getOwner();
}
