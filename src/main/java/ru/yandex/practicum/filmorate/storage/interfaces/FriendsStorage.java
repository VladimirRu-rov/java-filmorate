package ru.yandex.practicum.filmorate.storage.interfaces;

public interface FriendsStorage {
	void addFriends(Integer userId, Integer friendId);

	void deleteFriend(Integer userId, Integer friendId);
}
