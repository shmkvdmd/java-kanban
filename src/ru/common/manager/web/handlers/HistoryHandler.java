package ru.common.manager.web.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;

import java.io.IOException;

public final class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager) {
        super(taskManager);
    }

    private Endpoint getEndpoint(String requestPath, String requestMethod) {
        String[] pathParts = requestPath.split("/");

        if (pathParts.length == 2) {
            return switch (requestMethod) {
                case "GET" -> Endpoint.GET_HISTORY;
                default -> Endpoint.UNKNOWN;
            };
        }
        return Endpoint.UNKNOWN;
    }

    private void handleGetHistory(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getHistoryManager().getHistory());
        writeResponse(httpExchange, response, 200);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_HISTORY -> handleGetHistory(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, "Internal Server Error", 500);
        }
    }
}
