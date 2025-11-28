package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

	private final FilmStorage filmStorage;
	private final UserStorage userStorage;

	public Film likeFilm(Long filmId, Long userId) {
		log.debug("Попытка поставить лайк фильму. FilmID: {}, UserID: {}", filmId, userId);

		Film film = filmStorage.getFilmById(filmId);
		if (film == null) {
			log.warn("Фильм с ID {} не найден при попытке поставить лайк", filmId);
			throw new NotFoundException("Фильм не найден");
		}
		if (userStorage.getUserById(userId) == null) {
			log.warn("Пользователь с ID {} не найден при попытке поставить лайк", userId);
			throw new NotFoundException("Пользователь не найден");
		}
		if (film.getLikes().contains(userId)) {
			log.info("Пользователь {} уже поставил лайк фильму {}", userId, filmId);
			return film;
		}
		film.getLikes().add(userId);
		filmStorage.updateFilm(film);

		log.info("Лайк успешно добавлен. FilmID: {}, UserID: {}, текущее количество лайков: {}",
			filmId, userId, film.getLikes().size());

		return film;
	}


	public Film unlikeFilm(Long filmId, Long userId) {
		log.debug("Попытка снять лайк с фильма. FilmID: {}, UserID: {}", filmId, userId);

		Film film = filmStorage.getFilmById(filmId);
		if (film == null) {
			log.warn("Фильм с ID {} не найден при попытке снять лайк", filmId);
			throw new NotFoundException("Фильм не найден");
		}
		if (userStorage.getUserById(userId) == null) {
			log.warn("Пользователь с ID {} не найден при попытке снять лайк", userId);
			throw new NotFoundException("Пользователь не найден");
		}
		if (!film.getLikes().contains(userId)) {
			log.info("Пользователь {} не ставил лайк фильму {}", userId, filmId);
			return film;
		}
		film.getLikes().remove(userId);
		filmStorage.updateFilm(film);

		log.info("Лайк успешно снят. FilmID: {}, UserID: {}, оставшееся количество лайков: {}",
			filmId, userId, film.getLikes().size());

		return film;
	}

	public List<Film> getMostLikedFilms(int count) {
		if (count < 0) {
			throw new ValidationException("Количество фильмов не может быть отрицательным");
		}
		log.debug("Запрос топ-{} самых лайкнутых фильмов", count);

		Collection<Film> allFilms = filmStorage.showAllFilm();
		List<Film> mostLiked = allFilms.stream()
			.sorted((f1, f2) -> Integer.compare(
				f2.getLikes().size(),
				f1.getLikes().size()
			))
			.limit(count)
			.toList();

		log.info("Получено топ-{} самых лайкнутых фильмов. Найдено: {}", count, mostLiked.size());
		return mostLiked;
	}
}

