package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
	private final UserStorage userStorage;

	public void addFriends(Long userId, Long friendId) {
		User user = userStorage.getUserById(userId);
		User friend = userStorage.getUserById(friendId);

		if (user == null | friend == null) {
			log.debug("Ошибка: указан неверный ID. Пользователи не найдены при попытке добавления в друзья.");
			throw new NotFoundException(
				"Ошибка: указан неверный ID. Пользователи не найдены при попытке добавления в друзья.");
		}
		user.getFriends().add(friendId);
		friend.getFriends().add(userId);
		log.debug("Друг с ID '{}' успешно добавлен пользователю '{}'", friendId, userId);
		log.debug("Друг с ID '{}' успешно добавлен пользователю '{}'", userId, friendId);
	}

	public void deleteFriends(Long userId, Long friendId) {
		User user = userStorage.getUserById(userId);
		User friend = userStorage.getUserById(friendId);

		if (user == null || friend == null) {
			log.warn("Ошибка: пользователи с ID {} или {} не найдены", userId, friendId);
			throw new NotFoundException("Пользователи не найдены при удалении из друзей");
		}
		boolean userHasFriend = user.getFriends().contains(friendId);
		boolean friendHasUser = friend.getFriends().contains(userId);

		if (!userHasFriend || !friendHasUser) {
			log.warn("Предупреждение: отсутствие взаимной дружбы. Пользователь {}: {}, Пользователь {}: {}",
				userId, userHasFriend, friendId, friendHasUser);
		}
		user.getFriends().remove(friendId);
		friend.getFriends().remove(userId);
		log.info("Дружба между пользователями {} и {} успешно удалена", userId, friendId);
	}

	public Collection<User> showAllFriends(Long userId) {
		User user = userStorage.getUserById(userId);
		Collection<User> users = new ArrayList<>();
		for (Long friendsId : user.getFriends()) {
			users.add(userStorage.getUserById(friendsId));
		}

		log.info("Все друзья");
		return users;
	}

	public Collection<User> getCommonFriends(Long userId, Long otherUserId) {

		User user = userStorage.getUserById(userId);
		User otherUser = userStorage.getUserById(otherUserId);

		Set<Long> commonFriendIds = new HashSet<>(user.getFriends());
		commonFriendIds.retainAll(otherUser.getFriends());

		Collection<User> commonFriends = commonFriendIds.stream()
			.map(userStorage::getUserById)
			.toList();

		log.debug("Найден {} общих друзей для пользователей ID: {} и {}",
			commonFriends.size(), userId, otherUserId);

		return commonFriends;
	}
}

