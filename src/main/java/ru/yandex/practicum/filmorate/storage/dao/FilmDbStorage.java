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

@Repository("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaDbStorage;
    private final GenreDbStorage genreDbStorage;

    @Override
    public Film create(Film film) {
        validateMpa(film.getMpa());

        String insertSql = """
                INSERT INTO films (name, description, release_date, duration, mpa_id)
                VALUES (?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement =
                    connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            preparedStatement.setString(1, film.getName());
            preparedStatement.setString(2, film.getDescription());
            preparedStatement.setDate(3, Date.valueOf(film.getReleaseDate()));
            preparedStatement.setInt(4, film.getDuration());
            preparedStatement.setInt(5, film.getMpa().getId());
            return preparedStatement;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        insertFilmGenres(film.getId(), film.getGenres());
        film.setMpa(mpaDbStorage.findById(film.getMpa().getId()).orElse(film.getMpa()));
        return film;
    }

    @Override
    public Film save(Film film) {
        if (film.getId() == null || findById(film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден");
        }

        validateMpa(film.getMpa());

        String updateSql = """
                UPDATE films
                SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ?
                WHERE id = ?
                """;

        jdbcTemplate.update(updateSql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        insertFilmGenres(film.getId(), film.getGenres());
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                WHERE f.id = ?
                """;

        List<Film> films = jdbcTemplate.query(sql, (resultSet, rowNum) -> {
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

        return Optional.of(films.get(0));
    }

    @Override
    public List<Film> findAll() {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN mpa m ON f.mpa_id = m.id
                ORDER BY f.id
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNum) -> {
            Film film = new Film();
            film.setId(resultSet.getLong("id"));
            film.setName(resultSet.getString("name"));
            film.setDescription(resultSet.getString("description"));
            Date releaseDate = resultSet.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(resultSet.getInt("duration"));
            film.setMpa(new Mpa(resultSet.getInt("mpa_id"), resultSet.getString("mpa_name")));
            return film;
        });
    }

    private void validateMpa(Mpa mpa) {
        if (mpa == null || mpa.getId() == 0) {
            throw new NotFoundException("MPA не указан");
        }
        Integer exists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM mpa WHERE id = ?", Integer.class, mpa.getId());
        if (exists == null || exists == 0) {
            throw new NotFoundException("MPA с id=" + mpa.getId() + " не найден");
        }
    }

    private void insertFilmGenres(Long filmId, List<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        String existsSql = "SELECT COUNT(*) FROM genres WHERE id = ?";
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres.stream().distinct().toList()) {
            if (genre.getId() == null) continue;
            Integer exists = jdbcTemplate.queryForObject(existsSql, Integer.class, genre.getId());
            if (exists == null || exists == 0) {
                throw new NotFoundException("Жанр с id=" + genre.getId() + " не найден");
            }
            jdbcTemplate.update(insertSql, filmId, genre.getId());
        }
    }

    @Override
    public List<Film> findMostPopular(int count) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration,
                       f.mpa_id, m.name AS mpa_name
                FROM films f
                LEFT JOIN film_likes fl ON f.id = fl.film_id
                LEFT JOIN mpa m ON f.mpa_id = m.id
                GROUP BY f.id
                ORDER BY COUNT(fl.user_id) DESC
                LIMIT ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            Date releaseDate = rs.getDate("release_date");
            if (releaseDate != null) film.setReleaseDate(releaseDate.toLocalDate());
            film.setDuration(rs.getInt("duration"));
            film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            return film;
        }, count);
    }
}