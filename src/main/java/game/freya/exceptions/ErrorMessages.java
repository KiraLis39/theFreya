package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    UNIVERSAL_ERROR_MESSAGE_TEMPLATE("E0XX", ""),
    FIELD_NOT_FOUND("ER01", "Поле отсутствует или не заполнено %s"),
    ACCESS_DENIED("ER02", "Не хватает прав на выполнение запроса"),
    WRONG_DATA("ER03", "Данные не соответствуют требованиям %s"),
    JSON_PARSE_ERR("ER04", "Ошибка при парсинге JSON"),
    SYNC_ERROR("ER05", "Ошибка синхронизации между сервисами"),
    NOT_ENOUGH_PERMISSIONS("ER06", "У вас нет прав на выполнение данного метода"),
    NO_CONNECTION_REACHED("ER07", "Проблема с сетевым подключением"),
    WORLD_NOT_FOUND("ER08", "Не обнаружен в базе денных мир"),
    DRAW_TIMEOUT("ER09", "Не удалось отрисовать компонент за отведённое время"),
    PLAYER_NOT_FOUND("ER10", "Не обнаружен в базе денных игрок"),
    DRAW_ERROR("ER11", "В процессе рисования произошла ошибка"),
    RESOURCE_READ_ERROR("ER12", "Ошибка чтения ресурса игры"),
    WRONG_STATE("ER13", "Не верный статус приложения"),
    HERO_NOT_FOUND("ER14", "Не обнаружен Герой"),
    SOCKET_CLOSED("ER15", "Сокет закрыт и не может быть использован"),
    GL("ER16", "Ошибка OpenGL");

    private final String errorCode;

    private final String errorCause;
}
