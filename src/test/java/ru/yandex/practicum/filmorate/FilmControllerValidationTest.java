package ru.yandex.practicum.filmorate;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static ru.yandex.practicum.filmorate.model.Film.getMinDate;


@SpringBootTest
public class FilmControllerValidationTest {

	@Autowired
	private FilmController filmController;

	private Film film;

	@BeforeEach
	public void setUp() {
		film = new Film();
		film.setName("Valid Title");
		film.setDescription("Valid description");
		film.setReleaseDate(getMinDate().plusDays(1));
		film.setDuration(90L);
	}

	@Test
	public void whenNameIsNull() {
		film.setName(null);
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Название не может быть пустым.");
	}

	@Test
	public void whenNameIsEmpty() {
		film.setName("");
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Название не может быть пустым.");
	}

	@Test
	public void whenDescriptionTooLong() {
		film.setDescription("a".repeat(201));
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Описание не может быть длиннее 200 символов.");
	}

	@Test
	public void whenReleaseDateIsNull() {
		film.setReleaseDate(null);
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Дата релиза обязательна.");
	}

	@Test
	public void whenReleaseDateBeforeMin() {
		film.setReleaseDate(getMinDate().minusDays(1));
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Дата релиза слишком ранняя. Минимум: " + getMinDate());
	}

	@Test
	public void minReleaseDate() {
		film.setReleaseDate(getMinDate());
		filmController.validateFilm(film); // Не должно быть исключения
	}

	@Test
	public void whenDurationIsZero() {
		film.setDuration(0L);
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Продолжительность фильма не может быть отрицательной или нулевой.");
	}


	@Test
	public void whenDurationIsNegative() {
		film.setDuration(-10L);
		assertThatThrownBy(() -> filmController.validateFilm(film))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Продолжительность фильма не может быть отрицательной или нулевой.");
	}

	@Test
	public void whenDurationIsValid() {
		film.setDuration(1L); // Минимально допустимое значение
		filmController.validateFilm(film); // Не должно быть исключения
	}

	@Test
	public void shouldPassValidFilm() {
		filmController.validateFilm(film); // Не должно быть исключения
	}
}
