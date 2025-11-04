package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.ComponentScan;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.dao.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@ComponentScan(basePackages = {"ru.yandex.practicum.filmorate.storage.dao"})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserDbStorageTest {

    private final UserDbStorage userDbStorage;

    @Test
    void testCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@test.com");
        user.setLogin("tester");
        user.setName("Tester");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userDbStorage.create(user);

        Optional<User> fromDb = userDbStorage.findById(user.getId());
        assertThat(fromDb)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("email", "test@test.com"));
    }
}