package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

	private final Map<Long, Film> films = new HashMap<>();

	@Override
	public Collection<Film> showAllFilm() {
		log.info("Получен запрос на получение всех фильмов. Количество: {}", films.size());
		return films.values();
	}

	@Override
	public Film getFilmById(Long filmId) {
		if (filmId == null) {
			throw new ValidationException("ID фильма не может быть null");
		}
		Film film = films.get(filmId);
		if (film == null) {
			throw new NotFoundException("Фильм с ID " + filmId + " не найден");
		}
		return film;
	}

	@Override
	public Film addFilm(Film film) {
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

	@Override
	public Film updateFilm(Film newFilm) {
		if (newFilm.getId() == null) {
			throw new ValidationException("ID фильма не может быть null.");
		}
		if (!films.containsKey(newFilm.getId())) {
			throw new NotFoundException("Фильм с ID " + newFilm.getId() + " не найден.");
		}

		Film oldFilm = films.get(newFilm.getId());
		try {
			validateFilm(newFilm);

			oldFilm.setName(newFilm.getName());
			oldFilm.setDescription(newFilm.getDescription());
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
