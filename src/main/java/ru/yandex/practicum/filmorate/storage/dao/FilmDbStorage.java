package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            PreparedStatement prepareStatement = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            prepareStatement.setString(1, film.getName());
            prepareStatement.setString(2, film.getDescription());
            prepareStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            prepareStatement.setInt(4, film.getDuration());
            prepareStatement.setInt(5, film.getMpaRating().ordinal() + 1); // если используем id 1..5
            return prepareStatement;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key != null) film.setId(key.longValue());

        if (film.getGenreIds() != null && !film.getGenreIds().isEmpty()) {
            String insertGenre = "MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)";
            for (Integer gid : film.getGenreIds()) {
                jdbcTemplate.update(insertGenre, film.getId(), gid);
            }
        }
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public Film save(Film film) {
        if (findById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpaRating().ordinal() + 1, film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        if (film.getGenreIds() != null) {
            String insertGenre = "MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES (?, ?)";
            for (Integer gid : film.getGenreIds()) {
                jdbcTemplate.update(insertGenre, film.getId(), gid);
            }
        }
        return findById(film.getId()).orElseThrow();
    }

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa_id FROM films f";
        List<Film> films = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Film film = new Film();
            film.setId(resultSet.getLong("id"));
            film.setName(resultSet.getString("name"));
            film.setDescription(resultSet.getString("description"));
            Date releaseDate = resultSet.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(resultSet.getInt("duration"));
            int mpaId = resultSet.getInt("mpa_id");
            film.setMpaRating(MpaDbStorage.mpaFromId(mpaId));
            return film;
        });

        for (Film film : films) {
            film.setGenreIds(new HashSet<>(jdbcTemplate.queryForList("SELECT genre_id FROM film_genres WHERE film_id = ?", Integer.class, film.getId())));
        }
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT id, name, description, release_date, duration, mpa_id FROM films WHERE id = ?";
        List<Film> list = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Film film = new Film();
            film.setId(resultSet.getLong("id"));
            film.setName(resultSet.getString("name"));
            film.setDescription(resultSet.getString("description"));
            Date releaseDate = resultSet.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(resultSet.getInt("duration"));
            int mpaId = resultSet.getInt("mpa_id");
            film.setMpaRating(MpaDbStorage.mpaFromId(mpaId));
            return film;
        }, id);
        if (list.isEmpty()) return Optional.empty();
        Film film = list.get(0);
        List<Integer> genreIds = jdbcTemplate.queryForList("SELECT genre_id FROM film_genres WHERE film_id = ?", Integer.class, film.getId());
        film.setGenreIds(new HashSet<>(genreIds));
        return Optional.of(film);
    }
}