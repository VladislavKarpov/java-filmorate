package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service

public class UserService {
    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friendsMap = new HashMap<>();

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User create(User user) {
        log.info("Пользователь успешно создан: {}", user.getName());
        return userStorage.create(user);
    }

    public User update(User user) {
        log.info("Пользователь обновлён: {}", user.getId());
        return userStorage.update(user);
    }

    public List<User> findAll() {
        log.info("Запрос списка всех пользователей");
        List<User> users = userStorage.findAll();
        log.info("Найдено пользователей: {}", users.size());
        return users;
    }

    public User findById(Long id) {
        log.info("Поиск пользователя по id={}", id);
        User user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Добавление в друзья: userId={} friendId={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);

        // односторонняя заявка: userId -> friendId
        friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Удаление из друзей: userId={} friendId={}", userId, friendId);
        checkUserExists(userId);
        checkUserExists(friendId);

        // удаляем только связь userId -> friendId (т.к. дружба стала односторонней)
        friendsMap.getOrDefault(userId, new HashSet<>()).remove(friendId);
        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя id={}", userId);
        checkUserExists(userId);

        Set<Long> friendsIds = friendsMap.getOrDefault(userId, Set.of());
        return friendsIds.stream()
                .map(userStorage::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        log.info("Получение общих друзей пользователей {} и {}", userId, otherId);
        checkUserExists(userId);
        checkUserExists(otherId);

        Set<Long> userFriends = friendsMap.getOrDefault(userId, Set.of());
        Set<Long> otherFriends = friendsMap.getOrDefault(otherId, Set.of());

        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(userStorage::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    private void checkUserExists(Long id) {
        if (userStorage.findById(id).isEmpty()) {
            log.warn("Попытка обращения к несуществующему пользователю id={}", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }


}
