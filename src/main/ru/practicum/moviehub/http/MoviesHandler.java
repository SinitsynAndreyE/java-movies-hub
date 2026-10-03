package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends  BaseHttpHandler {
    private final MoviesServer server;

    public MoviesHandler(MoviesServer server) {
        this.server = server;
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        Endpoint endpoint = getEndpoint(ex.getRequestURI().getPath(), ex.getRequestMethod(), ex.getRequestURI().getQuery());

        switch (endpoint) {
            case GET_MOVIES:
                String jsonString = new Gson().toJson(server.getStore().getMovies().values());
                sendJson(ex,200,jsonString);
                break;
            case GET_MOVIES_BY_YEAR:
                String[] partsQuery = ex.getRequestURI().getQuery().split("=");
                try {
                    int id = Integer.parseInt(partsQuery[1]);
                    List<Movie> movies = server.getStore().getMoviesByYear(id);
                    sendJson(ex, 200, new Gson().toJson(movies));
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setError("Некорректный параметр запроса — 'year'");
                    sendJson(ex, 400, new Gson().toJson(errorResponse));
                }
            case GET_MOVIE:
                String stringId = ex.getRequestURI().getPath().split("/")[2];
                try {
                    int id = Integer.parseInt(stringId);
                    Movie movie = server.getStore().getMovie(id);
                    if (movie == null) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setError("Фильм не найден");
                        sendJson(ex, 404, new Gson().toJson(errorResponse));
                    } else {
                        sendJson(ex, 200, new Gson().toJson(movie));
                    }
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setError("Некорректный ID");
                    sendJson(ex, 400, new Gson().toJson(errorResponse));
                }
            case POST_MOVIE:
                Headers headers = ex.getRequestHeaders();
                if (!headers.containsKey("Content-Type") || !headers.get("Content-Type").getFirst().equals("application/json; charset=UTF-8")) {
                    sendNoContent(ex, 415);
                } else {
                    try (InputStream is = ex.getRequestBody()) {
                        String jsonBody = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonBody);
                        if (!jsonElement.getAsJsonObject().keySet().contains("title") || !jsonElement.getAsJsonObject().keySet().contains("year")) {
                            ErrorResponse errorResponse = new ErrorResponse();
                            errorResponse.setError("Некорректный JSON");
                            sendJson(ex, 422, new Gson().toJson(errorResponse));
                        } else {
                            Movie movie = new Gson().fromJson(jsonBody, Movie.class);
                            ErrorResponse errorResponse = new ErrorResponse();
                            if (isValidMovie(movie, errorResponse)) {
                                int id = server.getStore().addMovie(movie);
                                String jsonPostString = "{\"id\":\"" + id + "\"," + jsonBody.substring(1);
                                sendJson(ex, 201, jsonPostString);
                            } else {
                                sendJson(ex, 422, new Gson().toJson(errorResponse));
                            }
                        }
                    }
                }
                break;
            case DELETE_MOVIE:
                String stringDeleteId = ex.getRequestURI().getPath().split("/")[2];
                try {
                    int id = Integer.parseInt(stringDeleteId);
                    Movie movie = server.getStore().getMovie(id);
                    if (movie == null) {
                        ErrorResponse errorResponse = new ErrorResponse();
                        errorResponse.setError("Фильм не найден");
                        sendJson(ex, 404, new Gson().toJson(errorResponse));
                    } else {
                        server.getStore().deleteMovie(id);
                        sendNoContent(ex, 204);
                    }
                } catch (NumberFormatException e) {
                    ErrorResponse errorResponse = new ErrorResponse();
                    errorResponse.setError("Некорректный ID");
                    sendJson(ex, 400, new Gson().toJson(errorResponse));
                }
                break;
            default:
                sendNoContent(ex,405);
        }
    }

    private boolean isValidMovie(Movie movie, ErrorResponse errorResponse) {
        boolean isValid = true;
        if (movie.getTitle().length() > 100) {
            errorResponse.setError("Ошибка валидации");
            errorResponse.addDetails("заголовок должен быть меньше 100 символов");
            isValid = false;
        }

        if (movie.getTitle().isBlank()) {
            errorResponse.setError("Ошибка валидации");
            errorResponse.addDetails("заголовок не должен быть пустым");
            isValid = false;
        }

        if (movie.getYear() < 1888 || movie.getYear() > 2027) {
            errorResponse.setError("Ошибка валидации");
            errorResponse.addDetails("год должен быть между 1888 и 2027");
            isValid = false;
        }
        return isValid;
    }

    private Endpoint getEndpoint(String path, String method, String query) {
        String[] partsPath = path.split("/");
        switch (method) {
            case "GET":
                if (query == null) {
                    if (partsPath.length == 2) {
                        return Endpoint.GET_MOVIES;
                    } else if (partsPath.length == 3) {
                        return Endpoint.GET_MOVIE;
                    } else {
                        return Endpoint.UNKNOWN;
                    }
                } else {
                    String[] partsQuery = query.split("=");
                    if (partsQuery.length == 2 && partsQuery[0].equals("year")){
                        return Endpoint.GET_MOVIES_BY_YEAR;
                    } else {
                        return Endpoint.UNKNOWN;
                    }
                }
            case "POST":
                return Endpoint.POST_MOVIE;
            case "DELETE":
                return Endpoint.DELETE_MOVIE;
            default:
                return Endpoint.UNKNOWN;
        }
    }
}
