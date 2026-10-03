package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpServer;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MoviesServer {
    private final HttpServer server;
    private MoviesStore store;

    public MoviesStore getStore() {
        return store;
    }

    public void setStore(MoviesStore store) {
        this.store = store;
    }

    public MoviesServer(MoviesStore store, int port) {
        try {
            // создайте сервер
            this.store = store;
            server = HttpServer.create(new InetSocketAddress(port),0);
            server.createContext("/movies", new MoviesHandler(this));
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать HTTP-сервер", e);
        }
    }

    public void start() {
        // запустите сервер
        server.start();
        System.out.println("Сервер запущен");
    }

    public void stop() {
        // остановите сервер
        server.stop(0);
        System.out.println("Сервер остановлен");
    }
}