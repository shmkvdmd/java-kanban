package ru.common.manager.web.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.NotFoundException;
import ru.common.exceptions.constants.HttpMessageConstants;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;
import ru.common.models.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetSubtasks(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getAllSubtasks());
        writeResponse(httpExchange, response, 200);
    }

    private void handleGetSubtaskById(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
            return;
        }
        try {
            Subtask subtask = taskManager.getSubtaskById(idOpt.get());
            String response = gson.toJson(subtask);
            writeResponse(httpExchange, response, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
        }
    }

    private void handleCreateOrUpdateSubtask(HttpExchange httpExchange) throws IOException {
        try {
            String body = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Optional<Integer> optId = getTaskIdFromJson(body);
            if (optId.isEmpty()) {
                Subtask subtask = gson.fromJson(body, Subtask.class);
                taskManager.addSubtask(subtask);
                writeResponse(httpExchange, "", 201);
                return;
            }
            Subtask subtask = gson.fromJson(body, Subtask.class);
            subtask.setId(optId.get());
            taskManager.updateSubtask(subtask);
            writeResponse(httpExchange, "", 201);
        } catch (IllegalArgumentException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_ACCEPTABLE, 406);
        } catch (NotFoundException | IOException e) {
            writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }

    private void handleDeleteSubtask(HttpExchange httpExchange) throws IOException {
        Optional<Integer> idOpt = getTaskId(httpExchange);
        if (idOpt.isEmpty()) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
            return;
        }
        try {
            taskManager.deleteSubtaskById(idOpt.get());
            writeResponse(httpExchange, HttpMessageConstants.SUBTASK_DELETED, 200);
        } catch (NotFoundException e) {
            writeResponse(httpExchange, HttpMessageConstants.NOT_FOUND, 404);
        }
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = Endpoint.getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_SUBTASKS -> handleGetSubtasks(httpExchange);
            case Endpoint.GET_SUBTASK_BY_ID -> handleGetSubtaskById(httpExchange);
            case Endpoint.POST_SUBTASK -> handleCreateOrUpdateSubtask(httpExchange);
            case Endpoint.DELETE_SUBTASK -> handleDeleteSubtask(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }
}
