package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> idToUser = new HashMap<>();
    private long idCounter = 0L;

    private long generateNextId() {
        return ++idCounter;
    }


    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        long id = generateNextId();
        user.setId(id);
        idToUser.put(id, user);
        return user;
    }

    @Override
    public User update(User user) {
        Long id = user.getId();
        if (!idToUser.containsKey(id)) {
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        idToUser.put(id, user);
        return user;
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(idToUser.values());
    }

    @Override
    public Optional<User> findById(Long id) {
        User user = idToUser.get(id);
        return Optional.ofNullable(user);
    }
}
