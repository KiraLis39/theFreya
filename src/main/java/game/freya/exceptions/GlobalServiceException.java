package game.freya.exceptions;

/**
 * Исключение, которое используется повсеместно в сервисе.
 * В будущем, возможно, стоит несколько разделить события по разным типам исключений вместо одного.
 */
public class GlobalServiceException extends ExceptionsRoot {

    public GlobalServiceException(ErrorMessages error) {
        this(error.getErrorCause(), error.getErrorCode());
    }

    public GlobalServiceException(ErrorMessages error, Object data) {
        this(error.getErrorCause().contains("%s")
                ? error.getErrorCause().formatted(data)
                : error.getErrorCause() + ": " + data, error.getErrorCode());
    }

    public GlobalServiceException(String message, String errorCode) {
        super(message, errorCode);
    }

    public GlobalServiceException(ErrorMessages error, Exception e) {
        super(error.getErrorCause() + (e.getCause() == null ? e.getMessage()
                : e.getCause().getCause() == null ? e.getCause().getMessage()
                : e.getCause().getCause().getMessage()), error.getErrorCode());
    }
}
