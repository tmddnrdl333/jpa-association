package orm.exception;

public class EntityClassTypeNotInRelationException extends OrmPersistenceException {

    public EntityClassTypeNotInRelationException(String message) {
        super(message);
    }

    public EntityClassTypeNotInRelationException(String message, Throwable cause) {
        super(message, cause);
    }
}
