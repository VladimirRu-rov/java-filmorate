package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {
	private final MpaService mpaService;

	@GetMapping("/{mpaId}")
	public Mpa getMpa(@PathVariable Integer mpaId) {
		return mpaService.getMpa(mpaId);
	}

	@GetMapping
	public List<Mpa> getAllMpa() {
		return mpaService.getAllMpa();
	}
}
