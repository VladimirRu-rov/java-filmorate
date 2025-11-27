package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

	private final Map<Long, Film> films = new HashMap<>();

	@GetMapping
	public Collection<Film> showAllFilm() {
		log.info("Получен запрос на получение всех фильмов. Количество: {}", films.size());
		return films.values();
	}

	@PostMapping
	public Film addFilm(@Valid @RequestBody Film film) {
		try {
			validateFilm(film);
			long newId = getNextId();
			film.setId(newId);
			films.put(newId, film);
			log.info("Фильм создан. ID: {}, название: {}", newId, film.getName());
			return film;
		} catch (ValidationException e) {
			log.warn("Ошибка валидации при создании фильма: {}, данные: {}", e.getMessage(), film);
			throw e;
		}
	}

	@PutMapping
	public Film updateFilm(@Valid @RequestBody Film newFilm) {
		if (newFilm.getId() == null) {
			throw new ValidationException("ID фильма не может быть null.");
		}
		if (!films.containsKey(newFilm.getId())) {
			throw new NotFoundException("Фильм с ID " + newFilm.getId() + " не найден.");
		}

		Film oldFilm = films.get(newFilm.getId());
		try {
			validateFilm(newFilm);

			oldFilm.setName(newFilm.getName()); // Всегда обновляем
			oldFilm.setDescription(newFilm.getDescription()); // Всегда обновляем
			oldFilm.setReleaseDate(newFilm.getReleaseDate());
			oldFilm.setDuration(newFilm.getDuration());

			log.info("Фильм обновлён. ID: {}, новое название: {}", oldFilm.getId(), oldFilm.getName());
			return oldFilm;
		} catch (ValidationException e) {
			log.warn("Ошибка валидации при обновлении фильма ID {}: {}", newFilm.getId(), e.getMessage());
			throw e;
		}
	}

	private void validateFilm(Film film) {
		if (film.getReleaseDate().isBefore(Film.getMinDate())) {
			throw new ValidationException("Дата релиза слишком ранняя. Минимум: " + Film.getMinDate());
		}
	}

	private long getNextId() {
		long currentMaxId = films.keySet()
			.stream()
			.mapToLong(id -> id)
			.max()
			.orElse(0);
		return ++currentMaxId;
	}
}

