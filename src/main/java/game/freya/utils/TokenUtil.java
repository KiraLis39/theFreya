package game.freya.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import game.freya.exceptions.ErrorMessages;
import game.freya.exceptions.GlobalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenUtil {
    private final ObjectMapper mapper;

    public String getMailByToken(String token) throws IOException {
        if (token != null && !token.isEmpty() && token.split("\\.").length >= 2) {
            log.info("Поиск почты пользователя по ДТД...");
            return JsonUtils.getJsonValueOrNull(
                    mapper.readTree(Base64.getUrlDecoder().decode(token.split("\\.")[1])), "email");
        } else {
            log.warn("У опроса не указан mail.");
            throw new GlobalServiceException(ErrorMessages.FIELD_NOT_FOUND, "'mail'");
        }
    }
}
