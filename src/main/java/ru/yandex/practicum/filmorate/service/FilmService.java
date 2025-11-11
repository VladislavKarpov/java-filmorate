package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.dao.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.dao.LikeDbStorage;

import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;
    private final GenreDbStorage genreDbStorage;
    private final LikeDbStorage likeDbStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       UserService userService,
                       GenreDbStorage genreDbStorage,
                       LikeDbStorage likeDbStorage) {
        this.filmStorage = filmStorage;
        this.userService = userService;
        this.genreDbStorage = genreDbStorage;
        this.likeDbStorage = likeDbStorage;
    }

    public Film create(Film film) {
        Film created = filmStorage.create(film);
        enrichFilmWithGenres(List.of(created));
        log.info("Создан фильм: id={}, name={}", created.getId(), created.getName());
        return created;
    }

    public Film save(Film updatedFilm) {
        Film film = filmStorage.save(updatedFilm);
        enrichFilmWithGenres(List.of(film));
        log.info("Фильм обновлён: id={}, name={}", film.getId(), film.getName());
        return film;
    }

    public List<Film> findAll() {
        log.info("Получение списка всех фильмов");
        List<Film> films = filmStorage.findAll();
        enrichFilmWithGenres(films);
        return films;
    }

    public Film findById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден"));
        enrichFilmWithGenres(List.of(film));
        return film;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Добавление лайка фильму {} от пользователя {}", filmId, userId);

        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));

        userService.findById(userId); // проверка существования пользователя
        likeDbStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", filmId, userId);

        filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id=" + filmId + " не найден"));

        userService.findById(userId);
        likeDbStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        log.info("Получение {} самых популярных фильмов", count);
        List<Film> films = filmStorage.findMostPopular(count);
        enrichFilmWithGenres(films);
        return films;
    }

    private void enrichFilmWithGenres(List<Film> films) {
        if (films.isEmpty()) return;

        Map<Long, List<Genre>> filmGenres = genreDbStorage.findGenresForFilms(
                films.stream().map(Film::getId).toList()
        );

        for (Film film : films) {
            film.setGenres(filmGenres.getOrDefault(film.getId(), Collections.emptyList()));
        }
    }
}