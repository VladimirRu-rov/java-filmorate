package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserStorage userStorage;
	private final UserService userService;

	@GetMapping
	public Collection<User> showAllUsers() {
		return userStorage.showAllUsers();
	}

	@PostMapping
	public User addUser(@Valid @RequestBody User user) {
		return userStorage.addUser(user);
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User newUser) {
		return userStorage.updateUser(newUser);
	}

	@PutMapping("/{userId}/friends/{friendId}")
	public void addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
		userService.addFriends(userId, friendId);
	}

	@DeleteMapping("/{userId}/friends/{friendId}")
	public void deleteFriend(@PathVariable Long userId, @PathVariable Long friendId) {
		userService.deleteFriends(userId, friendId);
	}

	@GetMapping("/{userId}/friends")
	public Collection<User> getUserFriends(@PathVariable Long userId) {
		return userService.showAllFriends(userId);
	}

	@GetMapping("/{userId}/friends/common/{otherUserId}")
	public Collection<User> getCommonFriends(
		@PathVariable Long userId,
		@PathVariable Long otherUserId) {
		return userService.getCommonFriends(userId, otherUserId);
	}
}


