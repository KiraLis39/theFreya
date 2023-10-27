package game.freya.exceptions;

import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
//@RestControllerAdvice
public class ExceptionsHandler {
    @ExceptionHandler({Exception.class})
    public void handle(Exception e) {
        log.error("Handled: {}", ExceptionUtils.getFullExceptionMessage(e));
    }

//    @ExceptionHandler(value = {Exception.class})
//    public ResponseEntity<Object> handleAnyException(Exception ex, WebRequest request) {
//        log.warn("Service Exception {}", ExceptionUtils.getFullExceptionMessage(ex)
//                .concat(". Вызвано при: ").concat(request != null ? request.getContextPath() : "NA"));
//        return new ResponseEntity<>(
//                new UserErrorMessage(new Date(), "E000", ExceptionUtils.getFullExceptionMessage(ex)
//                        + (ExceptionUtils.getFullExceptionMessage(ex) != null
//                        ? ": " + ExceptionUtils.getFullExceptionMessage(ex) : "")),
//                new HttpHeaders(),
//                HttpStatus.INTERNAL_SERVER_ERROR);
//    }

//    @ExceptionHandler(value = {GlobalServiceException.class})
//    public ResponseEntity<Object> handleUserServiceException(GlobalServiceException ex, WebRequest request) {
//        log.warn("Business Exception: {}", ExceptionUtils.getFullExceptionMessage(ex)
//                .concat(". Вызвано при: ").concat(request != null ? request.getContextPath() : "NA"));
//        return new ResponseEntity<>(
//                new UserErrorMessage(new Date(), ex.getErrorCode(),
//                        ex.getErrorCode().equals("401") ? "" : ExceptionUtils.getFullExceptionMessage(ex)),
//                new HttpHeaders(),
//                HttpStatus.BAD_REQUEST);
//    }
}
