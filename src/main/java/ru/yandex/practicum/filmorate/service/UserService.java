package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserStorage userStorage;
	private final FriendsStorage friendsStorage;

	public List<User> showAllUsers() {
		log.debug("Запрос на получение всех пользователей");
		List<User> users = userStorage.showAllUsers();
		log.debug("Возвращено {} пользователей", users.size());
		return users;
	}

	public User addUser(User user) {
		log.debug("Добавление нового пользователя: email={}, login={}", user.getEmail(), user.getLogin());
		User addedUser = userStorage.addUser(user);
		log.info("Пользователь с ID={} успешно создан", addedUser.getId());
		return addedUser;
	}

	public User updateUser(User user) {
		log.debug("Обновление пользователя с ID={}", user.getId());
		Optional<User> optionalUser = userStorage.updateUser(user);

		if (optionalUser.isEmpty()) {
			log.warn("Пользователь с ID={} не найден при обновлении", user.getId());
			throw new NotFoundException("Пользователь с таким ID не существует при обновлении");
		}

		log.info("Пользователь с ID={} успешно обновлён", user.getId());
		return optionalUser.get();
	}

	public User getUserById(Integer userId) {
		log.debug("Получение пользователя по ID={}", userId);
		Optional<User> optionalUser = userStorage.getUserById(userId);

		if (optionalUser.isEmpty()) {
			log.warn("Пользователь с ID={} не найден", userId);
			throw new NotFoundException("Пользователь с ID «" + userId + "» не существует");
		}

		log.debug("Пользователь с ID={} найден: login='{}'", userId, optionalUser.get().getLogin());
		return optionalUser.get();
	}

	@Transactional
	public void addFriends(Integer userId, Integer friendId) {
		log.debug("Добавление дружбы: пользователь {} добавляет друга {}", userId, friendId);
		getUserById(userId);
		getUserById(friendId);

		friendsStorage.addFriends(userId, friendId);
		log.info("Друг с ID={} успешно добавлен пользователю с ID={}", friendId, userId);
	}

	public List<User> getUserFriends(Integer userId) {
		log.debug("Получение списка друзей для пользователя с ID={}", userId);
		getUserById(userId);
		List<User> friends = userStorage.getAllFriends(userId);
		log.debug("Найдено {} друзей для пользователя с ID={}", friends.size(), userId);
		return friends;
	}

	public List<User> getCommonFriendsId(Integer userId, Integer otherUserId) {
		log.debug("Поиск общих друзей между пользователем {} и {}", userId, otherUserId);
		getUserById(userId);
		getUserById(otherUserId);
		List<User> commonFriends = userStorage.getCommonFriendsIds(userId, otherUserId);
		log.debug("Найдено {} общих друзей между пользователем {} и {}", commonFriends.size(), userId, otherUserId);
		return commonFriends;
	}

	public User deleteUser(Integer userId) {
		log.info("Запрос на удаление пользователя с ID={}", userId);
		Optional<User> optionalUser = userStorage.deleteUser(userId);

		if (optionalUser.isEmpty()) {
			log.warn("Попытка удалить несуществующего пользователя с ID={}", userId);
			throw new NotFoundException("Пользователь с таким ID не существует при удалении");
		}

		User deletedUser = optionalUser.get();
		log.info("Пользователь с ID={} успешно удалён", deletedUser.getId());
		return deletedUser;
	}

	public void deleteFriend(Integer userId, Integer friendId) {
		log.debug("Запрос на удаление дружбы: пользователь {} удаляет друга {}", userId, friendId);
		getUserById(userId);
		getUserById(friendId);

		int beforeCount = userStorage.getAllFriends(userId).size();
		log.trace("Количество друзей у пользователя {} до удаления: {}", userId, beforeCount);

		friendsStorage.deleteFriend(userId, friendId);

		int afterCount = userStorage.getAllFriends(userId).size();
		log.trace("Количество друзей у пользователя {} после удаления: {}", userId, afterCount);

		log.info("Друг с ID={} успешно удалён у пользователя с ID={}", friendId, userId);
	}
}

