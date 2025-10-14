package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();


    public Film create(Film film) {
        filmStorage.create(film);
        return film;
    }


    public Film save(Film updatedFilm) {
        Long id = updatedFilm.getId();
        Optional<Film> optionalFilm = filmStorage.findById(id);
        if (optionalFilm.isEmpty()) {
            String errorMessage = String.format("Не найден фильм с %d", id);
            log.error(errorMessage);
            throw new NotFoundException(errorMessage);
        }
        updatedFilm = filmStorage.save(updatedFilm);
        return updatedFilm;
    }


    public List<Film> findAll() {
        return filmStorage.findAll();
    }


    public Film findById(Long id) {
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        if (filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
        if (userStorage.findById(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
        filmLikes.getOrDefault(filmId, new HashSet<>()).remove(userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.findAll().stream()
                .sorted((f1, f2) -> Integer.compare(
                        filmLikes.getOrDefault(f2.getId(), Set.of()).size(),
                        filmLikes.getOrDefault(f1.getId(), Set.of()).size()
                ))
                .limit(count)
                .collect(Collectors.toList());
    }
}
