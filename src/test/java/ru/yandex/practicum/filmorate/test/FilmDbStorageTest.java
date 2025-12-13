package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class FilmDbStorageTest {

	@Autowired
	private FilmStorage filmStorage;

	@Autowired
	private MpaStorage mpaStorage;

	@Autowired
	private GenreStorage genreStorage;

	private Film testFilm;

	@BeforeEach
	public void setUp() {
		Mpa mpa = mpaStorage.getMpa(1).orElseThrow(() -> new IllegalStateException("MPA с ID=1 не найден"));
		testFilm = Film.builder().name("Test Film").description("A test film description")
			.releaseDate(LocalDate.of(2020, 1, 1)).duration(120).mpa(mpa).genres(new HashSet<>()).build();
	}

	@Test
	public void addFilmSaveFilmToDatabase() {
		Film savedFilm = filmStorage.addFilm(testFilm);
		assertThat(savedFilm.getId()).isPositive();
		assertThat(savedFilm.getName()).isEqualTo(testFilm.getName());
		assertThat(savedFilm.getDuration()).isEqualTo(testFilm.getDuration());
		assertThat(savedFilm.getMpa().getId()).isEqualTo(testFilm.getMpa().getId());
		assertThat(savedFilm.getGenres()).isNotNull();
	}

	@Test
	public void addFilmWithInvalidReleaseDate() {
		testFilm.setReleaseDate(LocalDate.of(1894, 12, 27)); // раньше минимальной даты
		assertThatThrownBy(() -> filmStorage.addFilm(testFilm)).isInstanceOf(ValidationException.class)
			.hasMessageContaining("Дата релиза слишком ранняя");
	}

	@Test
	public void addFilmWithNonExistentMpa() {
		testFilm.setMpa(new Mpa(999, "Invalid MPA"));
		assertThatThrownBy(() -> filmStorage.addFilm(testFilm)).isInstanceOf(NotFoundException.class)
			.hasMessageContaining("MPA с ID=999 не найден");
	}

	@Test
	public void updateFilmExistingFilm() {
		Film savedFilm = filmStorage.addFilm(testFilm);
		savedFilm.setName("Updated Name");
		Optional<Film> updatedFilmOpt = filmStorage.updateFilm(savedFilm);
		assertThat(updatedFilmOpt).isPresent();
		Film updatedFilm = updatedFilmOpt.get();
		assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
	}

	@Test
	public void updateFilmOnNonExistentFilm() {
		testFilm.setId(999);
		Optional<Film> result = filmStorage.updateFilm(testFilm);
		assertThat(result).isEmpty();
	}

	@Test
	public void getFilmByIdReturnFilmIfExists() {
		Film savedFilm = filmStorage.addFilm(testFilm);
		Optional<Film> foundFilm = filmStorage.getFilmById(savedFilm.getId());
		assertThat(foundFilm).isPresent();
		assertThat(foundFilm.get().getName()).isEqualTo(testFilm.getName());
		assertThat(foundFilm.get().getGenres()).isNotNull();
	}

	@Test
	public void getFilmByIdOnNonExistentId() {
		Optional<Film> result = filmStorage.getFilmById(999);
		assertThat(result).isEmpty();
	}

	@Test
	public void showAllFilmReturnAllFilms() {
		filmStorage.addFilm(testFilm);
		testFilm.setName("Second Film");
		filmStorage.addFilm(testFilm);
		List<Film> films = filmStorage.showAllFilm();
		assertThat(films).hasSize(2);
		assertThat(films).extracting(Film::getName).containsExactlyInAnyOrder("Test Film", "Second Film");
	}

	@Test
	public void showAllFilmOnEmptyDatabase() {
		List<Film> films = filmStorage.showAllFilm();
		assertThat(films).isEmpty();
	}

	@Test
	public void deleteFilmRemoveFilmFromDatabase() {
		Film savedFilm = filmStorage.addFilm(testFilm);
		Integer filmId = savedFilm.getId();

		Optional<Film> deletedFilm = filmStorage.deleteFilm(filmId);

		assertThat(deletedFilm).isPresent();
		assertThat(deletedFilm.get().getId()).isEqualTo(filmId);
		Optional<Film> result = filmStorage.getFilmById(filmId);
		assertThat(result).isEmpty();
	}

	@Test
	public void deleteFilmOnNonExistentFilm() {
		Optional<Film> result = filmStorage.deleteFilm(999);
		assertThat(result).isEmpty();
	}

	@Test
	public void getMostPopularFilmWithZeroCount() {
		List<Film> result = filmStorage.getMostPopularFilm(0);
		assertThat(result).isEmpty();
	}

	@Test
	public void getMostPopularFilmWithNegativeCount() {
		List<Film> result = filmStorage.getMostPopularFilm(-5);
		assertThat(result).isEmpty();
	}

	@Test
	public void addFilmWithDuplicateGenreIds() {
		Genre genre1 = genreStorage.getGenre(1).orElseThrow(() -> new IllegalStateException("Жанр с ID=1 не найден"));
		Genre duplicateGenre = new Genre(1, "Любой жанр"); // тот же ID, но другой объект

		Set<Genre> genres = new HashSet<>(Arrays.asList(genre1, duplicateGenre));
		testFilm.setGenres(genres);

		assertThatThrownBy(() -> filmStorage.addFilm(testFilm)).isInstanceOf(ValidationException.class)
			.hasMessageContaining("Дублирующийся ID жанра");
	}

	@Test
	public void addFilmWithNonExistentGenre() {
		Genre invalidGenre = new Genre(999, "Unknown Genre");
		testFilm.setGenres(new HashSet<>(Collections.singletonList(invalidGenre)));

		assertThatThrownBy(() -> filmStorage.addFilm(testFilm)).isInstanceOf(NotFoundException.class)
			.hasMessageContaining("Жанр с ID=999 не найден");
	}

	@Test
	public void addFilmWithValidGenre() {
		Genre genre = genreStorage.getGenre(1).orElseThrow();
		testFilm.setGenres(new HashSet<>(Collections.singletonList(genre)));

		Film savedFilm = filmStorage.addFilm(testFilm);

		assertThat(savedFilm.getGenres()).hasSize(1);
		assertThat(savedFilm.getGenres()).extracting(Genre::getId).containsExactly(1);
	}
}