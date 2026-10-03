package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class MoviesStore {
    private Map<Integer, Movie> movies;

    public MoviesStore(Map<Integer, Movie> movies) {
        this.movies = movies;
    }

    public MoviesStore() {
        movies = Collections.emptyMap();
    }

    public Map<Integer, Movie> getMovies() {
        return movies;
    }

    public void setMovies(Map<Integer, Movie> movies) {
        this.movies = movies;
    }

    public int addMovie(Movie movie) {
        int id = movies.keySet().stream().max(Integer::compare).orElse(0) + 1;
        movie.setId(id);
        movies.put(id, movie);
        return id;
    }

    public Movie getMovie(int id) {
        return movies.getOrDefault(id, null);
    }

    public void deleteMovie(int id) {
        movies.remove(id);
    }

    public void clearMovies() {
        movies.clear();
    }

    public List<Movie> getMoviesByYear(int year) {
        return movies.values().stream()
                .filter(movie -> movie.getYear() == year)
                .toList();
    }

}