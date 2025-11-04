package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film create(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new NotFoundException("MPA не указан");
        }
        Integer mpaId = film.getMpa().getId();
        Integer mpaExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa WHERE id = ?", Integer.class, mpaId);
        if (mpaExists == null || mpaExists == 0) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден");
        }

        String insertFilmSql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(insertFilmSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setInt(5, mpaId);
            return preparedStatement;
        }, keyHolder);
        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());


        LinkedHashSet<Integer> incomingGenreIds = extractGenreIdsPreservingOrder(film);
        insertFilmGenres(film.getId(), incomingGenreIds);

        film.setMpa(mpaDbStorage.findById(mpaId).orElse(new Mpa(mpaId, null)));
        loadGenresIntoFilm(film);

        return film;
    }

    @Override
    public Film save(Film film) {

        if (film.getId() == null || findById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        if (film.getMpa() == null || film.getMpa().getId() == 0) {
            throw new NotFoundException("MPA не указан");
        }
        Integer mpaId = film.getMpa().getId();
        Integer mpaExists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM mpa WHERE id = ?", Integer.class, mpaId);
        if (mpaExists == null || mpaExists == 0) {
            throw new NotFoundException("MPA с id=" + mpaId + " не найден");
        }

        // Обновление полей фильма
        String updateFilmSql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(updateFilmSql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaId,
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        LinkedHashSet<Integer> incomingGenreIds = extractGenreIdsPreservingOrder(film);
        insertFilmGenres(film.getId(), incomingGenreIds);

        film.setMpa(mpaDbStorage.findById(mpaId).orElse(new Mpa(mpaId, null)));
        loadGenresIntoFilm(film);

        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String selectSql = """
                SELECT film.id, film.name, film.description, film.release_date, film.duration,
                       film.mpa_id, m.name AS mpa_name
                FROM films film
                LEFT JOIN mpa m ON film.mpa_id = m.id
                WHERE film.id = ?
                """;

        List<Film> films = jdbcTemplate.query(selectSql, (resultSet, rowNum) -> {
            Film film = new Film();
            film.setId(resultSet.getLong("id"));
            film.setName(resultSet.getString("name"));
            film.setDescription(resultSet.getString("description"));
            Date releaseDate = resultSet.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(resultSet.getInt("duration"));
            film.setMpa(new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name")));
            return film;
        }, id);

        if (films.isEmpty()) {
            return Optional.empty();
        }

        Film film = films.get(0);
        loadGenresIntoFilm(film);
        return Optional.of(film);
    }

    @Override
    public List<Film> findAll() {
        String selectAllSql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                """;

        List<Film> films = jdbcTemplate.query(selectAllSql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            Date releaseDate = rs.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            return film;
        });

        for (Film film : films) {
            loadGenresIntoFilm(film);
        }

        return films;
    }

    private LinkedHashSet<Integer> extractGenreIdsPreservingOrder(Film film) {
        LinkedHashSet<Integer> ids = new LinkedHashSet<>();
        if (film == null) return ids;

        List<Genre> incomingGenres = film.getGenres();
        if (incomingGenres != null && !incomingGenres.isEmpty()) {
            for (Genre genre : incomingGenres) {
                if (genre != null && genre.getId() != null) {
                    ids.add(genre.getId());
                }
            }
        }

        if (ids.isEmpty() && film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            for (Integer id : film.getGenreIds()) {
                if (id != null) ids.add(id);
            }
        }

        return ids;
    }

    private void insertFilmGenres(Long filmId, LinkedHashSet<Integer> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) return;

        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        String existsGenreSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        String existsPairSql = "SELECT COUNT(*) FROM film_genres WHERE film_id = ? AND genre_id = ?";

        for (Integer genreId : genreIds) {
            Integer genreExists = jdbcTemplate.queryForObject(existsGenreSql, Integer.class, genreId);
            if (genreExists == null || genreExists == 0) {
                throw new NotFoundException("Жанр с id=" + genreId + " не найден");
            }
            Integer pairExists = jdbcTemplate.queryForObject(existsPairSql, Integer.class, filmId, genreId);
            if (pairExists == null || pairExists == 0) {
                jdbcTemplate.update(insertSql, filmId, genreId);
            }
        }
    }

    private void loadGenresIntoFilm(Film film) {
        if (film == null || film.getId() == null) {
            film.setGenres(Collections.emptyList());
            film.setGenreIds(Collections.emptySet());
            return;
        }

        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        List<Genre> genres = jdbcTemplate.query(sql, (resultSet, rowNum) ->
                new Genre(resultSet.getInt("id"), resultSet.getString("name")), film.getId());

        film.setGenres(genres);
        LinkedHashSet<Integer> ids = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        film.setGenreIds(ids);
    }
}