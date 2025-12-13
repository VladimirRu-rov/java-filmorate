package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreStorage {

	Optional<Genre> getGenre(Integer genreId);

	List<Genre> getAllGenres();
}
