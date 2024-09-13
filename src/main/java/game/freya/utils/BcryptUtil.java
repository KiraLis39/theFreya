package game.freya.utils;

import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;


@Component
@AllArgsConstructor
public class BcryptUtil {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

    public String encode(CharSequence text) {
        String encrypted = encoder.encode(text);
        if (encoder.matches(text.toString(), encrypted)) {
            return (encrypted);
        }
        throw new GlobalServiceException(ErrorMessages.PASSWORD_ENCRYPT_ERROR);
    }

    public boolean checkPassword(String text, String stored_hash) {
        if (stored_hash == null || !stored_hash.startsWith("$2a$")) {
            throw new java.lang.IllegalArgumentException("Invalid hash provided for comparison");
        }
        return (BCrypt.checkpw(text, stored_hash));
    }
}
