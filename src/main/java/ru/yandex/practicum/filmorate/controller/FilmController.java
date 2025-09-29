package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final HashMap<Long, Film> idToFilm = new HashMap<>();
    private Long idCounter = 0L;

    private Long generateNextId() {
        return ++idCounter;
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        Long id = generateNextId();
        film.setId(id);
        idToFilm.put(id, film);
        log.info("Фильм добавлен: id={}, name='{}'", id, film.getName());
        return film;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен список фильмов: {}", idToFilm.size());
        return new ArrayList<>(idToFilm.values());
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {
        Long id = updatedFilm.getId();
        Film currentFilm = idToFilm.get(id);

        if (Objects.isNull(currentFilm)) {
            String errorMessage = String.format("Не найден фильм с %d", id);
            throw new IllegalArgumentException(errorMessage);
        }

        idToFilm.put(id, updatedFilm);

        log.info("Фильм обновлен: id={}, name='{}'", updatedFilm.getId(),
                updatedFilm.getName());

        return updatedFilm;

    }

}
