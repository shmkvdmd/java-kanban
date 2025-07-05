package ru.common.manager.web.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.common.exceptions.constants.HttpMessageConstants;
import ru.common.manager.TaskManager;
import ru.common.manager.web.handlers.endpoint.Endpoint;

import java.io.IOException;

public final class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager taskManager, Gson gson) {
        super(taskManager, gson);
    }

    private void handleGetHistory(HttpExchange httpExchange) throws IOException {
        String response = gson.toJson(taskManager.getHistoryManager().getHistory());
        writeResponse(httpExchange, response, 200);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Endpoint endpoint = Endpoint.getEndpoint(httpExchange.getRequestURI().getPath(), httpExchange.getRequestMethod());
        switch (endpoint) {
            case Endpoint.GET_HISTORY -> handleGetHistory(httpExchange);
            case Endpoint.UNKNOWN -> writeResponse(httpExchange, HttpMessageConstants.INTERNAL_SERVER_ERROR, 500);
        }
    }
}
