package ecart.exception;

public class StockDetailsNotFoundException extends RuntimeException {
    public StockDetailsNotFoundException(String message) {
        super(message);
    }
}
