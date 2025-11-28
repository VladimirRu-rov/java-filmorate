package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

	private final FilmStorage filmStorage;
	private final FilmService filmService;

	@GetMapping
	public Collection<Film> showAllFilm() {
		return filmStorage.showAllFilm();
	}

	@PostMapping
	public Film addFilm(@Valid @RequestBody Film film) {
		return filmStorage.addFilm(film);
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film newFilm) {
		return filmStorage.updateFilm(newFilm);
	}

	@PutMapping("/{filmId}/like/{userId}")
	public Film likeFilm(@PathVariable Long filmId, @PathVariable Long userId) {
		return filmService.likeFilm(filmId, userId);
	}

	@DeleteMapping("/{filmId}/like/{userId}")
	public Film unlikeFilm(@PathVariable Long filmId, @PathVariable Long userId) {
		return filmService.unlikeFilm(filmId, userId);
	}

	@GetMapping("/popular")
	public List<Film> getMostLikedFilms(@RequestParam(defaultValue = "10") int count) {
		if (count < 0) {
			throw new IllegalArgumentException("Количество фильмов не может быть отрицательным");
		}
		return filmService.getMostLikedFilms(count);
	}
}

