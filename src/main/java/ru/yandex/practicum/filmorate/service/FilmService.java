package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final Map<Long, Set<Long>> filmLikes = new HashMap<>();

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }


    public Film create(Film film) {
        log.info("Создание фильма: {}", film.getName());
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
        log.info("Фильм обновлён: id={}, name={}", updatedFilm.getId(), updatedFilm.getName());
        return updatedFilm;
    }


    public List<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }


    public Film findById(Long id) {
        log.info("Поиск фильма по id={}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм {} ← пользователь {}", filmId, userId);

        if (filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }

        userService.findById(userId);
        filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм {} → пользователь {}", filmId, userId);

        if (filmStorage.findById(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }

        userService.findById(userId);

        filmLikes.getOrDefault(filmId, new HashSet<>()).remove(userId);
        log.info("Пользователь {} убрал лайк у фильма {}", userId, filmId);
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
