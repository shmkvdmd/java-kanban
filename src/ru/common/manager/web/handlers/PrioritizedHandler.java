package ru.common.manager.web.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.constants.HttpMessageConstants;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;

import java.io.IOException;

public final class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetPrioritizedTasks(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getPrioritizedTasks());
        writeResponse(httpExchange, response, 200);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = Endpoint.getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_PRIORITIZED_TASKS -> handleGetPrioritizedTasks(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }
}
