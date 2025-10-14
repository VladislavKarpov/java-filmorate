package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Выполнен  запрос на создание фильма. Созданный фильм: {}", film);

        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Ошибка валидации: пустое имя фильма");
            throw new NotFoundException("Название фильма не может быть пустым");
        }

        return filmService.create(film);
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Получен список всех фильмов");
        return filmService.findAll();
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film updatedFilm) {

        log.info("Фильм обновлен: id={}, name='{}'", updatedFilm.getId(),
                updatedFilm.getName());

        return filmService.save(updatedFilm);

    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Проставление лайка фильму {} от пользователя {}", id, userId);

        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Удаление лайка у фильма {} от пользователя {}", id, userId);

        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Получение топ-{} популярных фильмов", count);

        return filmService.getMostPopularFilms(count);
    }

}
