package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Optional;

public interface LikeStorage {
	Film likeFilm(Integer filmId, Integer userId);

	Optional<Film> unlikeFilm(Integer filmId, Integer userId);

}
