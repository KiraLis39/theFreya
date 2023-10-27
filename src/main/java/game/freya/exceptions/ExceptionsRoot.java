package game.freya.exceptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionsRoot extends RuntimeException {

    private final String errorCode;

    public ExceptionsRoot(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
        log.warn("ERoot: handled exception: {} (code: {})", message, errorCode);
    }

    public String getErrorCode() {
        return errorCode;
    }
}
