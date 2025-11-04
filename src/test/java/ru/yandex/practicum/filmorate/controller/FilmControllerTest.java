package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        // Очищаем таблицы перед каждым тестом
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM mpa");
        jdbcTemplate.execute("DELETE FROM genres");
        jdbcTemplate.execute("DELETE FROM film_genres");

        // Создаем минимальные данные для MPA и жанров
        jdbcTemplate.update("INSERT INTO mpa (id, name) VALUES (?, ?)", 1, "G");
        jdbcTemplate.update("INSERT INTO genres (id, name) VALUES (?, ?)", 1, "Action");
    }

    @Test
    public void createFilmWhenValidData() throws Exception {
        Film film = new Film();
        film.setName("Uncharted");
        film.setDescription("Action adventure");
        film.setReleaseDate(LocalDate.of(2022, 2, 18));
        film.setDuration(116);
        film.setMpaRating(MpaRating.G);
        film.setGenreIds(Set.of(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldFailWhenDescriptionTooLong() throws Exception {
        Film film = new Film();
        film.setName("Long Film");
        film.setDescription("Too long description".repeat(100));
        film.setReleaseDate(LocalDate.of(2015, 8, 22));
        film.setDuration(190);
        film.setMpaRating(MpaRating.G);
        film.setGenreIds(Set.of(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailWhenReleaseDateTooEarly() throws Exception {
        Film film = new Film();
        film.setName("Very Old Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(1850, 4, 21));
        film.setDuration(90);
        film.setMpaRating(MpaRating.G);
        film.setGenreIds(Set.of(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldFailWhenDurationIsNegative() throws Exception {
        Film film = new Film();
        film.setName("Test negative duration");
        film.setDescription("Negative");
        film.setReleaseDate(LocalDate.of(2024, 5, 7));
        film.setDuration(-9);
        film.setMpaRating(MpaRating.G);
        film.setGenreIds(Set.of(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }
}