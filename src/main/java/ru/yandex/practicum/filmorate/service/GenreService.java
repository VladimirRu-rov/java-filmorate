package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreService {

	private final GenreStorage genreStorage;

	public Genre getGenre(Integer genreId) {
		Optional<Genre> genre = genreStorage.getGenre(genreId);

		if (genre.isEmpty()) {
			log.debug("Жанр с ID {} не найден", genreId);
			throw new NotFoundException("Жанр с ID " + genreId + " не найден");
		}

		log.debug("Жанр с ID {} успешно возвращён", genreId);
		return genre.get();
	}

	public Collection<Genre> getAllGenres() {
		List<Genre> genreList = genreStorage.getAllGenres();

		if (genreList.isEmpty()) {
			log.debug("Жанры не найдены");
			throw new NotFoundException("Жанры не найдены");
		}

		log.debug("Все жанры успешно возвращены");
		return genreList;
	}
}
