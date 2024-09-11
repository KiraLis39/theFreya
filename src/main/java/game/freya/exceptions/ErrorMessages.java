package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    UNIVERSAL_ERROR_MESSAGE_TEMPLATE("ERRX", "Неожиданная проблема"),

    // security:
    ACCESS_DENIED("S001", "Не хватает прав на выполнение запроса"),
    NOT_ENOUGH_PERMISSIONS("S002", "У вас нет прав на выполнение данного метода"),

    // data IO:
    FIELD_NOT_FOUND("IO01", "Поле отсутствует или не заполнено %s"),
    WRONG_DATA("IO02", "Данные не соответствуют требованиям %s"),
    NOT_ENOUGH_DATA("IO03", "Not enough data for processing"),
    JSON_PARSE_ERR("IO04", "Ошибка при парсинге JSON"),
    RESOURCE_READ_ERROR("IO05", "Ошибка чтения ресурса игры"),
    SYNC_ERROR("IO06", "Ошибка синхронизации между сервисами"),

    // draw UI:
    DRAW_TIMEOUT("UI01", "Не удалось отрисовать компонент за отведённое время"),
    DRAW_ERROR("UI02", "В процессе рисования произошла ошибка"),

    // net connections:
    NO_CONNECTION_REACHED("NT01", "Проблема с сетевым подключением"),
    SOCKET_CLOSED("NT02", "Сокет закрыт и не может быть использован"),

    // OS etc:
    WRONG_STATE("OS01", "Не верный статус приложения"),
    OS_NOT_SUPPORTED("OS02", "Operation system is not supported"),

    // game operations:,
    GAME_OPERATION_RESTRICTED("G001", "Запрошенная операция не прошла"),
    STORAGE_NOT_FOUND("G002", "Хранилище не найдено"),
    ITEM_NOT_FOUND("G003", "Item not exists?"),
    PLAYER_NOT_FOUND("G004", "Не обнаружен в базе данных игрок"),
    HERO_NOT_FOUND("G005", "Не обнаружен Герой"),
    WORLD_NOT_FOUND("G006", "Не обнаружен в базе денных мир"),
    NOT_ENOUGH_RESOURCES("G007", "Мало ресурсов для выполнения операции");

    private final String code;
    private final String errorCause;
}
