package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.dao.MpaDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final MpaDbStorage mpaDbStorage;

    @GetMapping
    public List<Mpa> findAll() {
        return mpaDbStorage.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> findById(@PathVariable int id) {
        return mpaDbStorage.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}