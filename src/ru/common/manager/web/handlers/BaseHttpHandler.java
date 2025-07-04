package ru.common.manager.web.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.common.exceptions.NotFoundException;
import ru.common.manager.TaskManager;
import ru.common.manager.web.jsonadapter.DurationAdapter;
import ru.common.manager.web.jsonadapter.LocalDateTimeAdapter;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

public abstract class BaseHttpHandler implements HttpHandler {
    protected final TaskManager taskManager;
    protected final Gson gson;

    public BaseHttpHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
    }

    Optional<Integer> getTaskId(HttpExchange httpExchange) {
        String[] pathParts = httpExchange.getRequestURI().getPath().split("/");
        try {
            return Optional.of(Integer.parseInt(pathParts[2]));
        } catch (NumberFormatException exception) {
            return Optional.empty();
        }
    }

    void writeResponse(HttpExchange httpExchange,
                       String responseString,
                       int responseCode) throws IOException {
        try (OutputStream os = httpExchange.getResponseBody()) {
            httpExchange.sendResponseHeaders(responseCode, 0);
            os.write(responseString.getBytes());
        }
    }

    Optional<Integer> getTaskIdFromJson(String body) throws IOException {
        JsonElement jsonElement = JsonParser.parseString(body);
        if (!jsonElement.isJsonObject()) {
            throw new NotFoundException("Ошибка парсинга задачи из JSON");
        }
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonElement idElement = jsonObject.get("id");
        if (idElement == null || idElement.isJsonNull()) {
            return Optional.empty();
        }
        return Optional.of(jsonObject.get("id").getAsInt());
    }
}
