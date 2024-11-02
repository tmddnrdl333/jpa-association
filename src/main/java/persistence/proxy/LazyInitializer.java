package persistence.proxy;

public interface LazyInitializer {
    Object getImplementation();

    boolean isInitialized();
}
