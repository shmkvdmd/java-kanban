package ru.common.manager.web.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.NotFoundException;
import ru.common.exceptions.constants.ExceptionMessageConstants;
import ru.common.exceptions.constants.HttpMessageConstants;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;
import ru.common.models.tasks.Task;
import ru.common.models.tasks.status.TaskStatus;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public final class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetTasks(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getAllTasks());
        writeResponse(httpExchange, response, 200);
    }

    private void handleGetTaskById(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
            return;
        }
        try {
            Task task = taskManager.getTaskById(idOpt.get());
            String response = gson.toJson(task);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
        }
    }

    private void handleCreateOrUpdateTask(HttpExchange httpExchange) throws IOException {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> optId = getTaskIdFromJson(body);
            if (optId.isEmpty()) {
                Task task = parseTaskFromJson(body);
                taskManager.addTask(task);
                writeResponse(httpExchange, "", 201);
                return;
            }
            Task task = parseTaskFromJson(body);
            task.setId(optId.get());
            taskManager.updateTask(task);
            writeResponse(httpExchange, "", 201);
        } catch (IllegalArgumentException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_ACCEPTABLE, 406);
        } catch (NotFoundException | IOException e) {
            writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }

    private Task parseTaskFromJson(String body) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(body);
        if (!jsonElement.isJsonObject()) {
            throw new NotFoundException(ExceptionMessageConstants.JSON_PARSE_ERROR);
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String taskName = jsonObject.get("taskName").getAsString();
        String taskDescription = jsonObject.get("taskDescription").getAsString();
        TaskStatus taskStatus = TaskStatus.valueOf(jsonObject.get("taskStatus").getAsString());
        LocalDateTime localDateTime = LocalDateTime.parse(jsonObject.get("startTime").getAsString());
        Duration duration = Duration.ofMinutes(jsonObject.get("duration").getAsInt());
        return new Task(taskName, taskDescription, taskStatus, localDateTime, duration);
    }

    private void handleDeleteTask(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
            return;
        }
        try {
            taskManager.deleteTaskById(idOpt.get());
            writeResponse(httpExchange, HttpMessageConstants.TASK_DELETED, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = Endpoint.getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_TASKS -> handleGetTasks(httpExchange);
            case Endpoint.GET_TASK_BY_ID -> handleGetTaskById(httpExchange);
            case Endpoint.POST_TASK -> handleCreateOrUpdateTask(httpExchange);
            case Endpoint.DELETE_TASK -> handleDeleteTask(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }
}
