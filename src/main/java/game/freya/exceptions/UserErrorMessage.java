package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class UserErrorMessage {
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    private String code;

    private String cause;
}
