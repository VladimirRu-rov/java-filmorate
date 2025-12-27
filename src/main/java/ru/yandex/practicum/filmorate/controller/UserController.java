package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

	private final UserService userService;

	@GetMapping
	public Collection<User> showAllUsers() {
		return userService.showAllUsers();
	}

	@PostMapping
	public User addUser(@Valid @RequestBody User user) {
		return userService.addUser(user);
	}

	@PutMapping
	public User updateUser(@Valid @RequestBody User user) {
		return userService.updateUser(user);
	}

	@DeleteMapping("/{userId}")
	private User deleteUser(@PathVariable Integer userId) {
		return userService.deleteUser(userId);
	}

	@GetMapping("/{userId}")
	public User getUserById(@PathVariable Integer userId) {
		return userService.getUserById(userId);
	}

	@PutMapping("/{userId}/friends/{friendId}")
	public void addFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
		userService.addFriends(userId, friendId);
	}

	@DeleteMapping("/{userId}/friends/{friendId}")
	public void deleteFriend(@PathVariable Integer userId, @PathVariable Integer friendId) {
		userService.deleteFriend(userId, friendId);
	}

	@GetMapping("/{userId}/friends")
	public List<User> getUserFriends(@PathVariable Integer userId) {
		return userService.getUserFriends(userId);
	}

	@GetMapping("/{userId}/friends/common/{otherUserId}")
	public Collection<User> getCommonFriends(@PathVariable Integer userId, @PathVariable Integer otherUserId) {
		return userService.getCommonFriendsId(userId, otherUserId);
	}
}


