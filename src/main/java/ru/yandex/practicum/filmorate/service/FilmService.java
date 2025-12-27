package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

	private final FilmStorage filmStorage;
	private final UserStorage userStorage;
	private final LikeStorage likeStorage;

	public Collection<Film> showAllFilm() {
		return filmStorage.showAllFilm();
	}

	public Film addFilm(Film film) {
		log.info("Фильм «{}» успешно добавлен", film.getName());
		return filmStorage.addFilm(film);
	}

	public Film updateFilm(Film film) {
		Optional<Film> optionalFilm = filmStorage.updateFilm(film);
		if (optionalFilm.isEmpty()) {
			log.debug("Ошибка некорректного ID: фильм с таким ID не существует при обновлении");
			throw new NotFoundException("Фильм с таким ID не существует при обновлении");
		}
		Film updatedFilm = optionalFilm.get();
		log.info("Фильм «{}» успешно обновлён", updatedFilm.getName());
		return updatedFilm;
	}

	public Film deleteFilm(Integer filmId) {
		log.debug("Попытка удаления фильма с ID: {}", filmId);

		Optional<Film> optionalFilm = filmStorage.deleteFilm(filmId);
		if (optionalFilm.isEmpty()) {
			log.debug("Ошибка: фильм с ID {} не найден при удалении", filmId);
			throw new NotFoundException("Фильм с таким ID не существует при удалении");
		}

		Film film = optionalFilm.get();
		log.debug("Фильм «{}» успешно удалён", film.getName());
		return film;
	}

	public Film likeFilm(Integer filmId, Integer userId) {
		getFilmById(filmId);
		Optional<User> optionalUser = userStorage.getUserById(userId);

		if (optionalUser.isEmpty()) {
			log.debug("Пользователь с ID {} не найден", userId);
			throw new NotFoundException("Пользователь с таким ID не существует");
		}
		likeStorage.likeFilm(filmId, userId);

		Film updatedFilm = getFilmById(filmId);
		log.debug("Фильм с ID {} оценён лайком пользователем ID {}", filmId, userId);
		return updatedFilm;
	}

	public Film unlikeFilm(Integer filmId, Integer userId) {
		Optional<Film> updatedFilm = likeStorage.unlikeFilm(filmId, userId);

		if (updatedFilm.isEmpty()) {
			log.debug("Лайк от пользователя ID={} к фильму ID={} не найден", userId, filmId);
			throw new NotFoundException("Лайк от пользователя ID=" + userId + " к фильму ID=" + filmId + " не найден");
		}

		log.debug("Фильм с ID «{}» успешно снят с лайка пользователем «{}»", filmId, userId);
		return updatedFilm.get();
	}

	public List<Film> getMostLikedFilms(Integer count) {
		return filmStorage.getMostPopularFilm(count);
	}

	public Film getFilmById(Integer filmId) {
		Optional<Film> optionalFilm = filmStorage.getFilmById(filmId);

		if (optionalFilm.isEmpty()) {
			log.debug("Ошибка некорректного ID: фильм с таким ID не существует при получении по ID");
			throw new NotFoundException("Фильм с таким ID не существует при получении по ID");
		}
		log.debug("Фильм с ID «{}» успешно возвращён", filmId);
		return optionalFilm.get();
	}
}


