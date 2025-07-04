package ru.common.manager.web.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.NotFoundException;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;
import ru.common.models.tasks.Subtask;
import ru.common.models.tasks.status.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public final class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_SUBTASKS;
                case "POST" -> Endpoint.POST_SUBTASK;
                default -> Endpoint.UNKNOWN;
            };
        }
        if (pathParts.length == 3) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_SUBTASK_BY_ID;
                case "DELETE" -> Endpoint.DELETE_SUBTASK;
                default -> Endpoint.UNKNOWN;
            };
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetSubtasks(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getAllSubtasks());
        writeResponse(httpExchange, response, 200);
    }

    private void handleGetSubtaskById(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, "Not Found", 404);
            return;
        }
        try {
            Subtask subtask = taskManager.getSubtaskById(idOpt.get());
            String response = gson.toJson(subtask);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, "Not Found", 404);
        }
    }

    // Возможно подобные методы также вынести в базовый класс с помощью дженериков
    private void handleCreateOrUpdateSubtask(HttpExchange httpExchange) throws IOException {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> optId = getTaskIdFromJson(body);
            if (optId.isEmpty()) {
                Subtask subtask = parseSubtaskFromJson(body);
                taskManager.addSubtask(subtask);
                writeResponse(httpExchange, "", 201);
                return;
            }
            Subtask subtask = parseSubtaskFromJson(body);
            subtask.setId(optId.get());
            taskManager.updateSubtask(subtask);
            writeResponse(httpExchange, "", 201);
        } catch (IllegalArgumentException e) {
            writeResponse(httpExchange, "Not Acceptable", 406);
        } catch (NotFoundException | IOException e) {
            writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }

    private Subtask parseSubtaskFromJson(String body) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(body);
        if (!jsonElement.isJsonObject()) {
            throw new NotFoundException("Ошибка парсинга задачи из JSON");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        TaskStatus taskStatus = TaskStatus.valueOf(jsonObject.get("taskStatus").getAsString());
        LocalDateTime localDateTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
        Duration duration = Duration.ofMinutes(jsonObject.get("duration").getAsInt());
        int epicId = jsonObject.get("epicId").getAsInt();
        return new Subtask(taskName, taskDescription, taskStatus, localDateTime, duration, epicId);
    }

    private void handleDeleteSubtask(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, "Not Found", 404);
            return;
        }
        try {
            taskManager.deleteSubtaskById(idOpt.get());
            writeResponse(httpExchange, "Подзадача удалена", 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, "Not Found", 404);
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_SUBTASKS -> handleGetSubtasks(httpExchange);
            case Endpoint.GET_SUBTASK_BY_ID -> handleGetSubtaskById(httpExchange);
            case Endpoint.POST_SUBTASK -> handleCreateOrUpdateSubtask(httpExchange);
            case Endpoint.DELETE_SUBTASK -> handleDeleteSubtask(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }
}
