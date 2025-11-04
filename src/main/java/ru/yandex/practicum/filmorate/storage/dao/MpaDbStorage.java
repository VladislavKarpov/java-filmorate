package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> findAll() {
        return jdbcTemplate.queryForList("SELECT id, name FROM mpa ORDER BY id");
    }

    public Optional<MpaRating> findById(int id) {
        List<MpaRating> list = jdbcTemplate.query("SELECT name FROM mpa WHERE id = ?", (resultSet, rn) -> MpaRating.fromString(resultSet.getString("name")), id);
        return list.stream().findFirst();
    }

    public static MpaRating mpaFromId(int id) {
        return switch (id) {
            case 1 -> MpaRating.G;
            case 2 -> MpaRating.PG;
            case 3 -> MpaRating.PG_13;
            case 4 -> MpaRating.R;
            case 5 -> MpaRating.NC_17;
            default -> throw new IllegalArgumentException("Unknown mpa id: " + id);
        };
    }
}