package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exceptions.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;
    private final Map<Long, Set<Long>> friendsMap = new HashMap<>();

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        return userStorage.update(user);
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        User user = userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден"));
        return user;
    }

    public void addFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        friendsMap.computeIfAbsent(userId, k -> new HashSet<>()).add(friendId);
        friendsMap.computeIfAbsent(friendId, k -> new HashSet<>()).add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        checkUserExists(userId);
        checkUserExists(friendId);

        friendsMap.getOrDefault(userId, new HashSet<>()).remove(friendId);
        friendsMap.getOrDefault(friendId, new HashSet<>()).remove(userId);
    }

    public List<User> getFriends(Long userId) {
        checkUserExists(userId);

        Set<Long> friendsIds = friendsMap.getOrDefault(userId, Set.of());
        return friendsIds.stream()
                .map(userStorage::findById)
                .flatMap(Optional::stream)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
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
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
    }


}
