package orm.life_cycle;

public interface EntityEntry {
    Status getStatus();

    boolean isStatus(Status status);

    EntityEntry updateStatus(Status status);

    Object getId();
}
