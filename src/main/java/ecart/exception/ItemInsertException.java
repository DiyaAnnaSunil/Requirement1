package ecart.exception;


public class ItemInsertException extends RuntimeException {

    public ItemInsertException() {
        super();
    }

    public ItemInsertException(String message) {
        super(message);
    }

    public ItemInsertException(String message, Throwable cause) {
        super(message, cause);
    }

    public ItemInsertException(Throwable cause) {
        super(cause);
    }
}
