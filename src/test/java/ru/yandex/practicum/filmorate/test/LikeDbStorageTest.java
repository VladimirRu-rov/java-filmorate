package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class LikeDbStorageTest {

	@Autowired
	private LikeStorage likeStorage;

	@Autowired
	private FilmStorage filmStorage;
	@Autowired
	private UserStorage userStorage;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	private Film testFilm;
	private User testUser;

	@BeforeEach
	public void setUp() {
		testUser =
			User.builder().email("user1@yandex.ru").login("user1").name("User One").birthday(LocalDate.of(1990, 1, 1))
				.build();
		testUser = userStorage.addUser(testUser);

		Mpa mpa = new Mpa(1, "G");
		testFilm =
			Film.builder().name("Test Film").description("A test description").releaseDate(LocalDate.of(2020, 1, 1))
				.duration(100).mpa(mpa).genres(Set.of()).build();
		testFilm = filmStorage.addFilm(testFilm);
	}

	@Test
	public void addLikeToFilm() {
		likeStorage.likeFilm(testFilm.getId(), testUser.getId());

		Integer likeCount =
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"like_list\" WHERE FILM_ID = ?", Integer.class,
				testFilm.getId());
		assertThat(likeCount).isEqualTo(1);
	}

	@Test
	public void removeExistingLike() {
		likeStorage.likeFilm(testFilm.getId(), testUser.getId());
		Optional<Film> result = likeStorage.unlikeFilm(testFilm.getId(), testUser.getId());

		assertThat(result).isPresent();
		Integer likeCount =
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"like_list\" WHERE FILM_ID = ?", Integer.class,
				testFilm.getId());
		assertThat(likeCount).isZero();
	}

	@Test
	public void returnEmptyOptionalForNonExistingLike() {
		Optional<Film> result = likeStorage.unlikeFilm(testFilm.getId(), 999);
		assertThat(result).isEmpty();
	}
}
