package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikeDbStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final LikeDbStorage likeDbStorage;
    private final FriendDbStorage friendDbStorage;

    public FilmService(
            @Qualifier("filmDbStorage") FilmStorage filmStorage,
            UserService userService,
            LikeDbStorage likeDbStorage,
            FriendDbStorage friendDbStorage
    ) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.likeDbStorage = likeDbStorage;
        this.friendDbStorage = friendDbStorage;
    }

    public Film create(Film film) {
        log.info("Создание фильма: {}", film.getName());
        Film created = filmStorage.create(film);
        log.info("Фильм успешно создан: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film save(Film updatedFilm) {
        Long id = updatedFilm.getId();
        if (id == null || filmStorage.findById(id).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + id + " не найден");
        }
        Film saved = filmStorage.save(updatedFilm);
        log.info("Фильм обновлён: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    public List<Film> findAll() {
        log.info("Получение списка всех фильмов");
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        log.info("Поиск фильма по id={}", id);
        return filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка: фильм {} ← пользователь {}", filmId, userId);
        Film film = findById(filmId);
        userService.findById(userId);
        likeDbStorage.addLike(filmId, userId);
        log.info("Лайк успешно добавлен пользователем {} фильму {}", userId, film.getName());
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка: фильм {} → пользователь {}", filmId, userId);
        findById(filmId);
        userService.findById(userId);
        likeDbStorage.removeLike(filmId, userId);
        log.info("Лайк успешно удалён пользователем {} у фильма {}", userId, filmId);
    }

    public List<Film> getMostPopularFilms(int count) {
        log.info("Получение списка самых популярных фильмов (топ {})", count);
        return filmStorage.findMostPopular(count);
    }
}