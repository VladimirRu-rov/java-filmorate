package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
public class UserControllerValidationTest {

	@Autowired
	private UserController userController;

	private User user;

	@BeforeEach
	public void setUp() {
		user = new User();
		user.setEmail("user@example.com");
		user.setLogin("validlogin");
		user.setName("John");
		user.setBirthday(LocalDate.of(1990, 1, 1));
	}

	@Test
	public void whenEmailIsNull() {
		user.setEmail(null);
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Электронная почта не может быть пустой и должна содержать символ @");
	}

	@Test
	public void whenEmailIsEmpty() {
		user.setEmail("");
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Электронная почта не может быть пустой и должна содержать символ @");
	}

	@Test
	public void whenEmailMissingAtSymbol() {
		user.setEmail("invalid-email");
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Электронная почта не может быть пустой и должна содержать символ @");
	}

	@Test
	public void whenEmailIsValidWithSubdomain() {
		user.setEmail("user@sub.example.com");
		userController.validateUser(user); // Не должно быть исключения
	}

	@Test
	public void whenLoginIsNull() {
		user.setLogin(null);
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Логин не может быть пустым и содержать пробелы");
	}

	@Test
	public void whenLoginContainsSpace() {
		user.setLogin("bad login");
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Логин не может быть пустым и содержать пробелы");
	}

	@Test
	public void whenLoginIsValidWithSpecialChars() {
		user.setLogin("user_123");
		userController.validateUser(user); // Не должно быть исключения
	}

	@Test
	public void setNameFromLoginWhenNameIsNullOrWhitespace() {
		// Проверка для null
		user.setName(null);
		userController.validateUser(user);
		assertThat(user.getName()).isEqualTo(user.getLogin());

		// Проверка для пробельного значения
		user.setName("   ");
		userController.validateUser(user);
		assertThat(user.getName()).isEqualTo(user.getLogin());
	}

	@Test
	public void nameDoesNotChangeIfAlreadySet() {
		String originalName = "John";
		user.setName(originalName);
		userController.validateUser(user);
		assertThat(user.getName()).isEqualTo(originalName); // Имя не должно измениться
	}

	@Test
	public void whenBirthdayIsNull() {
		user.setBirthday(null);
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Дата рождения обязательна");
	}

	@Test
	public void whenBirthdayInFuture() {
		user.setBirthday(LocalDate.now().plusDays(1));
		assertThatThrownBy(() -> userController.validateUser(user))
			.isInstanceOf(ValidationException.class)
			.hasMessage("Дата рождения не может быть в будущем.");
	}

	@Test
	public void acceptTodayAsBirthday() {
		user.setBirthday(LocalDate.now());
		userController.validateUser(user); // Не должно быть исключения
	}

	@Test
	public void shouldPassValidUser() {
		userController.validateUser(user); // Не должно быть исключения
	}
}
