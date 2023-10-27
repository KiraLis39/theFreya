package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UserErrorMessage {
    private Date timestamp;
    private String errorCode;
    private String errorCause;
}
