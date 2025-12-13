package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {

	List<User> showAllUsers();

	User addUser(User user);

	Optional<User> updateUser(User user);

	Optional<User> getUserById(Integer id);

	Optional<User> deleteUser(Integer userId);

	List<User> getAllFriends(Integer userId);

	List<User> getCommonFriendsIds(Integer userId, Integer otherUserId);
}