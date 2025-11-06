package ru.yandex.practicum.filmorate.model;


import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
public class Film {

    private Long id;

    @NotBlank(message = "Название фильма не может быть пустым.")
    private String name;

    @NotBlank(message = "Описание фильма не может быть пустым.")
    @Size(max = 200, message = "Описание не может превышать 200 символов.")
    private String description;

    @PastOrPresent(message = "Дата релиза не может быть в будущем времени")
    private LocalDate releaseDate;

    @AssertTrue(message = "Дата релиза не может быть раньше 28.12.1895")
    public boolean isReleaseDateValid() {
        return releaseDate != null && !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private Integer duration;

    private List<Genre> genres;

    @NotNull(message = "У фильма должен быть рейтинг")
    private Mpa mpa;
}
