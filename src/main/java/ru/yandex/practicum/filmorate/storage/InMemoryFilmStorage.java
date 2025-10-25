package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;


@Repository
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> idToFilm = new HashMap<>();
    private long idCounter = 0L;


    @Override
    public Film create(Film film) {
        long nextId = generateNextId();
        film.setId(nextId);
        idToFilm.put(nextId, film);
        return film;
    }

    @Override
    public Film save(Film film) {
        idToFilm.put(film.getId(), film);
        return film;
    }

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(idToFilm.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        Film film = idToFilm.get(id);
        return Optional.ofNullable(film);
    }

    private long generateNextId() {
        return ++idCounter;
    }
}
