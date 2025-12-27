package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;

import java.util.Optional;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

	private final JdbcTemplate jdbcTemplate;
	private final FilmStorage filmStorage;

	@Override
	public Film likeFilm(Integer filmId, Integer userId) {
		log.debug("Пользователь с ID={} ставит лайк фильму с ID={}", userId, filmId);

		String insertLike = "INSERT INTO \"like_list\" (FILM_ID, USER_ID) VALUES (?, ?)";
		try {
			jdbcTemplate.update(insertLike, filmId, userId);
			log.info("Лайк от пользователя ID={} добавлен к фильму ID={}", userId, filmId);
		} catch (Exception e) {
			log.warn("Не удалось добавить лайк: пользователь ID={} уже поставил лайк фильму ID={}", userId, filmId);
			throw e;
		}

		return filmStorage.getFilmById(filmId).orElseThrow(() -> {
			log.error("Фильм с ID={} не найден при попытке поставить лайк", filmId);
			return new IllegalArgumentException("Фильм с ID=" + filmId + " не найден");
		});
	}

	@Override
	public Optional<Film> unlikeFilm(Integer filmId, Integer userId) {
		log.debug("Попытка удаления лайка: пользователь ID={} удаляет лайк у фильма ID={}", userId, filmId);

		String checkLike = "SELECT COUNT(*) FROM \"like_list\" WHERE FILM_ID = ? AND USER_ID = ?";
		int count = jdbcTemplate.queryForObject(checkLike, Integer.class, filmId, userId);

		if (count == 0) {
			log.debug("Лайк не найден: пользователь ID={} не ставил лайк фильму ID={}", userId, filmId);
			return Optional.empty();
		}

		String deleteLike = "DELETE FROM \"like_list\" WHERE FILM_ID = ? AND USER_ID = ?";
		jdbcTemplate.update(deleteLike, filmId, userId);

		log.info("Лайк от пользователя ID={} успешно удалён у фильма ID={}", userId, filmId);

		return filmStorage.getFilmById(filmId);
	}
}

