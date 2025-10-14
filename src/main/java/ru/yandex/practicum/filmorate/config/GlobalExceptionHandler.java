package ru.yandex.practicum.filmorate.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public Map<String, String> handleInvalidArgumentException(RuntimeException exception) {
        return new HashMap<>();
    }


}
