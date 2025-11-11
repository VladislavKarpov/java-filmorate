package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public Optional<Genre> findById(int id) {
        String sql = "SELECT id, name FROM genres WHERE id = ?";
        List<Genre> result = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                id);
        return result.stream().findFirst();
    }

    public List<Genre> findAll() {
        String sql = "SELECT id, name FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("id"), rs.getString("name")));
    }

    public List<Genre> findGenresByFilmId(Long filmId) {
        String sql = """
                SELECT g.id, g.name
                FROM genres g
                JOIN film_genres fg ON g.id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.id
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Genre(rs.getInt("id"), rs.getString("name")),
                filmId);
    }

    public Map<Long, List<Genre>> findGenresForFilms(List<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Collections.emptyMap();
        }

        String sql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                ORDER BY g.id
                """.formatted(filmIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));

        Map<Long, List<Genre>> filmGenresMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre(rs.getInt("id"), rs.getString("name"));
            filmGenresMap.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });

        return filmGenresMap;
    }
}