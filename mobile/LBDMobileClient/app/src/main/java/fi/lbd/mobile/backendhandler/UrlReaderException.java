package fi.lbd.mobile.backendhandler;

/**
 * Exception from the url reader.
 *
 * Created by Tommi.
 */
public class UrlReaderException extends Exception {
    private final Exception originalException;

    public UrlReaderException(String message, Exception originalException) {
        super(message);
        this.originalException = originalException;
    }

    public Exception getOriginalException() {
        return this.originalException;
    }
}
