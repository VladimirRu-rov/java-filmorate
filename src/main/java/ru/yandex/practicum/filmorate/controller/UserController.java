package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

	private final Map<Long, User> users = new HashMap<>();

	@GetMapping
	public Collection<User> showAllUsers() {
		log.info("Получен запрос на получение всех пользователей. Количество: {}", users.size());
		return users.values();
	}

	@PostMapping
	public User addUser(@RequestBody User user) {
		try {
			validateUser(user);
			long newId = getNextId();
			user.setId(newId);
			users.put(newId, user);
			log.info("Пользователь создан ID: {}, логин: {},", newId, user.getLogin());
			return user;
		} catch (ValidationException e) {
			log.warn("Ошибка при создании пользователя: {} ", e.getMessage());
			throw e;
		}
	}

	@PutMapping
	public User updateUser(@RequestBody User newUser) {
		if (newUser.getId() == null) {
			log.warn("Попытка обновления пользователя без указания Id.");
			throw new ValidationException("Id должен быть указан.");
		}
		if (!users.containsKey(newUser.getId())) {
			log.warn("Пользователь с ID: {} не найден", newUser.getId());
			throw new NotFoundException("Пользователь с ID " + newUser.getId() + " не найден.");
		}


		User oldUser = users.get(newUser.getId());
		try {
			validateUser(newUser);
			oldUser.setName(newUser.getName());
			oldUser.setLogin(newUser.getLogin());
			oldUser.setEmail(newUser.getEmail());
			oldUser.setBirthday(newUser.getBirthday());

			log.info("Пользователь обновлён. ID: {}, Новое имя: {}", newUser.getId(), oldUser.getName());
			return oldUser;
		} catch (ValidationException e) {
			log.warn("Ошибка при обновлении пользователя c ID: {}: {}", newUser.getId(), e.getMessage());
			throw e;
		}
	}

	public void validateUser(User user) {
		if (user.getEmail() == null || user.getEmail().trim().isEmpty() || !user.getEmail().contains("@")) {
			throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
		}
		if (user.getLogin() == null || user.getLogin().trim().isEmpty() || user.getLogin().contains(" ")) {
			throw new ValidationException("Логин не может быть пустым и содержать пробелы");
		}
		if (user.getBirthday() == null) {
			throw new ValidationException("Дата рождения обязательна");
		}
		if (user.getBirthday().isAfter(LocalDate.now())) {
			throw new ValidationException("Дата рождения не может быть в будущем.");
		}

		// Подстановка login если имя пустое
		if (user.getName() == null || user.getName().trim().isEmpty()) {
			user.setName(user.getLogin());
		}
	}

	private long getNextId() {
		long currentMaxId = users.keySet()
			.stream()
			.mapToLong(id -> id)
			.max()
			.orElse(0);
		return ++currentMaxId;
	}
}


