package ru.common.exceptions.constants;

public final class ExceptionMessageConstants {
    public static final String NOT_FOUND_TASK = "Задача не найдена";
    public static final String NOT_FOUND_EPIC = "Эпик не найден";
    public static final String NOT_FOUND_SUBTASK = "Подзадача не найдена";

    public static final String ADD_WRONG_TYPE = "Не удалось добавить задачу. Неверный тип";
    public static final String UPDATE_WRONG_TYPE = "Не удалось обновить задачу. Неверный тип";

    public static final String INTERSECTION = "Задачи пересекаются";

    public static final String UNKNOWN_TYPE = "Неизвестный тип задачи";

    public static final String FILE_READ_ERROR = "Ошибка при чтении данных из файла";
    public static final String FILE_SAVE_ERROR = "Ошибка сохранения записи в файл";
    public static final String FILE_NULL = "Файл не создан";

    public static final String JSON_PARSE_ERROR = "Ошибка получения задачи из JSON";
}