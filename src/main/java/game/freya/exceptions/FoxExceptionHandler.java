package game.freya.exceptions;

import game.freya.utils.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
@RestControllerAdvice
public class FoxExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UserErrorMessage> handleAnyException(Exception ex, WebRequest request) {
        String errorMessage = ExceptionUtils.getFullExceptionMessage(ex);
        log.error(errorMessage.concat(". Вызвано при: ")
                .concat(request != null ? request.getDescription(false) : "NA"));
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(UserErrorMessage.builder().code("E000").cause(errorMessage).build());
    }

    @ExceptionHandler(GlobalServiceException.class)
    public ResponseEntity<UserErrorMessage> handleGlobalServiceException(GlobalServiceException ex, WebRequest request) {
        log.warn(ExceptionUtils.getFullExceptionMessage(ex).concat(". Вызвано при: ")
                .concat(request != null ? request.getDescription(false) : "NA"));
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(UserErrorMessage.builder().code(ex.getCode()).cause(ExceptionUtils.getFullExceptionMessage(ex)).build());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>(ex.getBindingResult().getErrorCount());
        ex.getBindingResult().getAllErrors().forEach(error -> errors.put(((FieldError) error).getField(), error.getDefaultMessage()));
        return errors;
    }
}
