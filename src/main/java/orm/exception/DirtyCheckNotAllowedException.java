package orm.exception;

public class DirtyCheckNotAllowedException extends OrmPersistenceException {

    public DirtyCheckNotAllowedException(String message) {
        super(message);
    }

    public DirtyCheckNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
