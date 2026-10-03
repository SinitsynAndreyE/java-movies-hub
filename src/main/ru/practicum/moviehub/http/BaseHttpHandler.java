package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8"; // !!! Укажите содержимое заголовка Content-Type

    @Override
    public abstract void handle(HttpExchange ex) throws IOException;

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        // !!! Реализуйте общий для всех хендлеров метод
        // для отправки ответа с телом в формате JSON
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status,0);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(json.getBytes());
        }
    }

    protected void sendNoContent(HttpExchange ex, int status) throws java.io.IOException {
        // !!! Реализуйте общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 204
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status,-1);
    }
}