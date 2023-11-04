package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    UNIVERSAL_ERROR_MESSAGE_TEMPLATE("E0XX", ""),
    FIELD_NOT_FOUND("ER01", "Поле отсутствует или не заполнено: %s"),
    ACCESS_DENIED("ER02", "Не хватает прав на выполнение запроса"),
    WRONG_DATA("ER03", "Данные не соответствуют требованиям: %s"),
    JSON_PARSE_ERR("ER04", "Ошибка при парсинге JSON"),
    SYNC_ERROR("ER05", "Ошибка синхронизации между сервисами"),
    NOT_ENOUGH_PERMISSIONS("ER06", "У вас нет прав на выполнение данного метода"),
    NO_CONNECTION_REACHED("ER07", "Не было получено ни одного соединения за отведённое время");

    private final String errorCode;
    private final String errorCause;
}
