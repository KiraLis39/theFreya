package game.freya.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorMessages {
    UNIVERSAL_ERROR_MESSAGE_TEMPLATE("E0XX", ""),
    NEW_ADMIN_EX("ER01", "findByRespondentId возращает более 1 значения"),
    RESPONDENT_IS_ABSENT("ER02", "Респондент не найден с uuid "),
    NULL_REQUEST_STATUS_ERROR("ER03", "Форма заявки не может быть без статуса"),
    EXCEL_CREATING_ERROR("ER04", "Ошибка при формировании Excel "),
    SURVEY_NOT_FOUND("ER05", "Не найден опрос "),
    NO_RESPONDENT("ER06", "Респонденты не обнаружены"),
    FIELD_NOT_FOUND("ER07", "Поле отсутствует или не заполнено: %s"),
    ANSWERS_PARSE_ERROR("ER08", "Не удалось распарсить JSON ответов "),
    ANSWER_NOT_FOUND("ER09", "Не найден ответ "),
    ACCESS_DENIED("401", "Не хватает прав на выполнение запроса"),
    EXCEL_DEL_ERROR("ER09", "Удаление excel файла произошло с ошибкой"),
    VALIDATION_ERROR("ER010", "Ошибка валидации: %s"),
    WRONG_DATA("ER011", "Данные не соответствуют требованиям: %s"),
    EVENT_NOT_FOUND("ER012", "Мероприятие не обнаружено"),
    JSON_PARSE_ERR("ER013", "Ошибка при парсинге JSON"),
    SYNC_ERROR("ER014", "Ошибка синхронизации между сервисами"),
    NOT_ENOUGH_PERMISSIONS("ER015", "У вас нет прав на выполнение данного метода");

    private final String errorCode;
    private final String errorCause;
}
