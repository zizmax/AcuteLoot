package acute.loot;

/**
 * Wrapper exception for exceptions raised in AcuteLoot.
 */
public class AcuteLootException extends RuntimeException {
    public AcuteLootException() {
        super();
    }

    public AcuteLootException(String message) {
        super(message);
    }

    public AcuteLootException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcuteLootException(Throwable cause) {
        super(cause);
    }

    protected AcuteLootException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
