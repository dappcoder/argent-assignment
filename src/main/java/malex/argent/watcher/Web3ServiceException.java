package malex.argent.watcher;

public class Web3ServiceException extends RuntimeException {

    public Web3ServiceException(String message) {
        super(message);
    }

    public Web3ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
