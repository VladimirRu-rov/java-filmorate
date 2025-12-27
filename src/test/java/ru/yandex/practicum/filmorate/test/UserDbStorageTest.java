package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserDbStorageTest {

	@Autowired
	private UserStorage userStorage;

	private User testUser;

	@BeforeEach
	public void setUp() {
		testUser = User.builder().email("test@yandex.ru").login("testuser").name("Test User")
			.birthday(LocalDate.of(1990, 1, 1)).build();
	}

	@Test
	public void saveUser() {
		User savedUser = userStorage.addUser(testUser);

		assertThat(savedUser.getId()).isPositive();
		assertThat(savedUser.getEmail()).isEqualTo(testUser.getEmail());
		assertThat(savedUser.getLogin()).isEqualTo(testUser.getLogin());
		assertThat(savedUser.getBirthday()).isEqualTo(testUser.getBirthday());
	}

	@Test
	public void returnUserById() {
		User savedUser = userStorage.addUser(testUser);
		Optional<User> foundUser = userStorage.getUserById(savedUser.getId());

		assertThat(foundUser).isPresent();
		assertThat(foundUser.get().getName()).isEqualTo(testUser.getName());
	}

	@Test
	public void returnEmptyForNonExistingId() {
		Optional<User> result = userStorage.getUserById(999);

		assertThat(result).isEmpty();
	}

	@Test
	public void updateUser() {
		User savedUser = userStorage.addUser(testUser);
		savedUser.setName("Updated Name");
		savedUser.setEmail("updated@yandex.ru");

		Optional<User> updatedUser = userStorage.updateUser(savedUser);

		assertThat(updatedUser).isPresent();
		assertThat(updatedUser.get().getName()).isEqualTo("Updated Name");
		assertThat(updatedUser.get().getEmail()).isEqualTo("updated@yandex.ru");
	}

	@Test
	public void returnEmptyOnUpdateNonExistingUser() {
		testUser.setId(999);
		Optional<User> result = userStorage.updateUser(testUser);

		assertThat(result).isEmpty();
	}

	@Test
	public void returnAllUsers() {
		userStorage.addUser(testUser);
		testUser.setEmail("second@yandex.ru");
		testUser.setLogin("seconduser");
		userStorage.addUser(testUser);


		List<User> users = userStorage.showAllUsers();

		assertThat(users).hasSize(2);
		assertThat(users).extracting(User::getEmail).contains("test@yandex.ru", "second@yandex.ru");
	}

	@Test
	public void returnEmptyListWhenNoUsers() {
		List<User> users = userStorage.showAllUsers();

		assertThat(users).isEmpty();
	}

	@Test
	public void deleteUser() {
		User savedUser = userStorage.addUser(testUser);
		Integer userId = savedUser.getId();

		Optional<User> deletedUser = userStorage.deleteUser(userId);

		assertThat(deletedUser).isPresent();
		assertThat(deletedUser.get().getId()).isEqualTo(userId);

		Optional<User> result = userStorage.getUserById(userId);
		assertThat(result).isEmpty();
	}

	@Test
	public void returnEmptyOnDeleteNonExistingUser() {
		Optional<User> result = userStorage.deleteUser(999);

		assertThat(result).isEmpty();
	}
}
