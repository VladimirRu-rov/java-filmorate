package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class GenreDbStorageTest {

	@Autowired
	private GenreStorage genreStorage;

	@Test
	public void returnExistingGenre() {
		Optional<Genre> result = genreStorage.getGenre(1);

		assertThat(result).isPresent();
		Genre genre = result.get();
		assertThat(genre.getId()).isEqualTo(1);
		assertThat(genre.getName()).isEqualTo("Комедия");
	}

	@Test
	public void returnEmptyForInvalidId() {
		Optional<Genre> result = genreStorage.getGenre(999);

		assertThat(result).isEmpty();
	}

	@Test
	public void returnAllSixGenres() {
		var genres = genreStorage.getAllGenres();

		assertThat(genres).hasSize(6);
		assertThat(genres.get(0).getName()).isEqualTo("Комедия");
		assertThat(genres.get(1).getName()).isEqualTo("Драма");
		assertThat(genres.get(2).getName()).isEqualTo("Мультфильм");
		assertThat(genres.get(3).getName()).isEqualTo("Триллер");
		assertThat(genres.get(4).getName()).isEqualTo("Документальный");
		assertThat(genres.get(5).getName()).isEqualTo("Боевик");
	}
}
