package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final HashMap<Long, User> idToUser = new HashMap<>();
    private Long idCounter = 0L;

    private long generateNextId() {
        return ++idCounter;
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        long id = generateNextId();
        user.setId(id);
        idToUser.put(id, user);

        log.info("Пользователь создан: id={}, login='{}'", id, user.getLogin());
        return user;

    }

    @PutMapping
    public User update(@Valid @RequestBody User updatedUser) {
        Long id = updatedUser.getId();
        User currentUser = idToUser.get(id);

        if (Objects.isNull(currentUser)) {
            String errorMessage = String.format("Не найден пользователь с %d", id);
            throw new IllegalArgumentException(errorMessage);
        }

        idToUser.put(id, updatedUser);

        log.info("Пользователь обновлен: id={}, login='{}'", updatedUser.getId(),
                updatedUser.getLogin());

        return updatedUser;

    }

    @GetMapping
    public List<User> getAll() {
        log.info("Получен список всех пользователей: {}", idToUser.size());
        return new ArrayList<>(idToUser.values());
    }

}