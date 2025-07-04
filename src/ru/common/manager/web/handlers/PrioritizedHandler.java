package ru.common.manager.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;

import java.io.IOException;

public final class PrioritizedHandler extends BaseHttpHandler {
    public PrioritizedHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_PRIORITIZED_TASKS;
                default -> Endpoint.UNKNOWN;
            };
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetPrioritizedTasks(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getPrioritizedTasks());
        writeResponse(httpExchange, response, 200);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_PRIORITIZED_TASKS -> handleGetPrioritizedTasks(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }
}
