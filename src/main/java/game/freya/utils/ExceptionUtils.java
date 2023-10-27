package game.freya.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class ExceptionUtils {
    private ExceptionUtils() {
    }

    public static String getFullExceptionMessage(Throwable t) {
        return t.getCause() == null
                ? t.getMessage() : t.getCause().getCause() == null
                ? t.getCause().getMessage() : t.getCause().getCause().getMessage();
    }
}
