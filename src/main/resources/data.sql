INSERT INTO mpa (id, name)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

INSERT INTO genres (id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

INSERT INTO users (email, login, name, birthday)
VALUES ('alice@mail.com', 'alice', 'Алиса', '1990-05-12'),
       ('bob@mail.com', 'bob', 'Боб', '1985-09-25'),
       ('charlie@mail.com', 'charlie', 'Чарли', '2000-02-10');

INSERT INTO films (name, description, release_date, duration, mpa_id)
VALUES ('Начало', 'Фильм Кристофера Нолана о снах и сознании.', '2010-07-16', 148, 4),
       ('Игры разума', 'История гениального математика Джона Нэша.', '2001-12-21', 135, 3);

INSERT INTO film_genres (film_id, genre_id)
VALUES (1, 4),
       (2, 2);