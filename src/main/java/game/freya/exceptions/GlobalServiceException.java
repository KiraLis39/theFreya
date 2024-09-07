package game.freya.exceptions;

import game.freya.utils.ExceptionUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

/**
 * Исключение, которое используется повсеместно в сервисе.
 * В будущем, возможно, стоит несколько разделить события по разным типам исключений вместо одного.
 */
@Slf4j
@Getter
public class GlobalServiceException extends RuntimeException {
    private final String code;

    public GlobalServiceException(String message, String code) {
        super(message);
        this.code = code;
        log.warn(message);
    }

    public GlobalServiceException(ErrorMessages error) {
        this(error.getErrorCause(), error.getCode());
    }

    public GlobalServiceException(ErrorMessages error, String data) {
        this(error.getErrorCause().concat(": ").concat(data), error.getCode());
    }

    public GlobalServiceException(ErrorMessages error, Throwable t) {
        this(error.getErrorCause() + ": " + ExceptionUtils.getFullExceptionMessage(t), error.getCode());
    }

    public GlobalServiceException(ErrorMessages error, String data, Throwable t) {
        this(error.getErrorCause().concat(": ").concat(data)
                        .concat(Arrays.toString(t.getCause().getStackTrace())) + ExceptionUtils.getFullExceptionMessage(t),
                error.getCode());
    }
}
