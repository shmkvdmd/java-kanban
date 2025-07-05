package ru.common.manager.web.handlers.endpoint;

public enum Endpoint {
    GET_TASKS("/tasks", "GET"),
    POST_TASK("/tasks", "POST"),
    GET_TASK_BY_ID("/tasks", "GET", true),
    DELETE_TASK("/tasks", "DELETE", true),

    GET_SUBTASKS("/subtasks", "GET"),
    POST_SUBTASK("/subtasks", "POST"),
    GET_SUBTASK_BY_ID("/subtasks", "GET", true),
    DELETE_SUBTASK("/subtasks", "DELETE", true),

    GET_EPICS("/epics", "GET"),
    GET_EPIC_BY_ID("/epics", "GET", true),
    GET_EPIC_SUBTASKS("/epics", "GET"),
    POST_EPIC("/epics", "POST"),
    DELETE_EPIC("/epics", "DELETE"),

    GET_HISTORY("/history", "GET"),
    GET_PRIORITIZED_TASKS("/prioritized", "GET"),
    UNKNOWN("", "");

    private final String path;
    private final String method;
    private final boolean containsId;

    Endpoint(String path, String method, boolean containsId) {
        this.path = path;
        this.method = method;
        this.containsId = containsId;
    }

    Endpoint(String path, String method) {
        this(path, method, false);
    }

    public static Endpoint getEndpoint(String requestPath, String requestMethod) {
        for (Endpoint endpoint : values()) {
            if (requestPath.startsWith(endpoint.path)
                    && requestMethod.equalsIgnoreCase(endpoint.method)) {
                if (endpoint.containsId && requestPath.split("/").length == 3) {
                    return endpoint;
                }
                if (endpoint.containsId && requestPath.split("/").length == 4) {
                    return endpoint;
                }
                if (!endpoint.containsId && requestPath.split("/").length == 2) {
                    return endpoint;
                }
            }
        }
        return UNKNOWN;
    }
}
