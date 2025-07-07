package ru.common;

import ru.common.manager.Managers;
import ru.common.manager.web.HttpTaskServer;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        HttpTaskServer httpTaskServer = new HttpTaskServer(Managers.getDefault());
        httpTaskServer.startServer();
    }
}
