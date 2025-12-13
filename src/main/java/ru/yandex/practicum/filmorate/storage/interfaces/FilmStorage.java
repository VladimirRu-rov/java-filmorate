package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

	List<Film> showAllFilm();

	Film addFilm(Film film);

	Optional<Film> updateFilm(Film film);

	Optional<Film> getFilmById(Integer filmId);

	Optional<Film> deleteFilm(Integer filmId);

	List<Film> getMostPopularFilm(Integer count);
}
