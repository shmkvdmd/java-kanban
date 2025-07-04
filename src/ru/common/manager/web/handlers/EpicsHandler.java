package ru.common.manager.web.handlers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.NotFoundException;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;
import ru.common.models.tasks.Epic;
import ru.common.models.tasks.status.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_EPICS;
                case "POST" -> Endpoint.POST_EPIC;
                default -> Endpoint.UNKNOWN;
            };
        }
        if (pathParts.length == 3) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_EPIC_BY_ID;
                case "DELETE" -> Endpoint.DELETE_TASK;
                default -> Endpoint.UNKNOWN;
            };
        }
        if (pathParts.length == 4) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_EPIC_SUBTASKS;
                default -> Endpoint.UNKNOWN;
            };
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetEpics(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getAllEpics());
        writeResponse(httpExchange, response, 200);
    }

    private void handleGetEpicById(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, "Not Found", 404);
            return;
        }
        try {
            Epic epic = taskManager.getEpicById(idOpt.get());
            String response = gson.toJson(epic);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, "Not Found", 404);
        } catch (Exception e) {
            e.printStackTrace();
            writeResponse(httpExchange, "Internal Server Error: " + e.getMessage(), 500);
        }
    }

    // Возможно подобные методы также вынести в базовый класс с помощью дженериков
    private void handleCreateEpic(HttpExchange httpExchange) throws IOException {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> optId = getTaskIdFromJson(body);
            if (optId.isEmpty()) {
                Epic epic = parseEpicFromJson(body);
                taskManager.addEpic(epic);
                writeResponse(httpExchange, "", 201);
            }
        } catch (IllegalArgumentException e) {
            writeResponse(httpExchange, "Not Acceptable", 406);
        } catch (NotFoundException | IOException e) {
            writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }

    private Epic parseEpicFromJson(String body) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(body);
        if (!jsonElement.isJsonObject()) {
            throw new NotFoundException("Ошибка парсинга задачи из JSON");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        TaskStatus taskStatus = TaskStatus.valueOf(jsonObject.get("taskStatus").getAsString());
        return new Epic(taskName, taskDescription, taskStatus);
    }

    private void handleDeleteEpic(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, "Not Found", 404);
            return;
        }
        try {
            taskManager.deleteEpicById(idOpt.get());
            writeResponse(httpExchange, "Эпик удален", 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, "Not Found", 404);
        }
    }

    private void handleGetEpicSubtasks(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, "Not Found", 404);
            return;
        }
        try {
            String response = gson.toJson(taskManager.getEpicSubtasks(idOpt.get()));
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, "Not Found", 404);
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_EPICS -> handleGetEpics(httpExchange);
            case Endpoint.GET_EPIC_BY_ID -> handleGetEpicById(httpExchange);
            case Endpoint.POST_EPIC -> handleCreateEpic(httpExchange);
            case Endpoint.DELETE_EPIC -> handleDeleteEpic(httpExchange);
            case Endpoint.GET_EPIC_SUBTASKS -> handleGetEpicSubtasks(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }
}
