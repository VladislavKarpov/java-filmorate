package ru.yandex.practicum.filmorate.storage.dao;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

@Repository("mpaDbStorage")
@RequiredArgsConstructor
public class MpaDbStorage {

    private final JdbcTemplate jdbcTemplate;

    public List<Mpa> findAll() {
        String sql = "SELECT id, name FROM mpa ORDER BY id";
        return jdbcTemplate.query(sql, (rs, rn) -> new Mpa(rs.getInt("id"), rs.getString("name")));
    }

    public Optional<Mpa> findById(int id) {
        String sql = "SELECT id, name FROM mpa WHERE id = ?";
        List<Mpa> list = jdbcTemplate.query(sql, (rs, rn) -> new Mpa(rs.getInt("id"), rs.getString("name")), id);
        return list.stream().findFirst();
    }
}