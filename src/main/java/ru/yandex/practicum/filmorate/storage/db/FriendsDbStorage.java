package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendsStorage;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FriendsDbStorage implements FriendsStorage {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public void addFriends(Integer userId, Integer friendId) {
		log.debug("Добавление дружбы: пользователь {} добавляет друга {}", userId, friendId);

		String insertFriend = "INSERT INTO \"friend_list\" (USER_ID, FRIEND_ID) VALUES (?, ?)";
		try {
			jdbcTemplate.update(insertFriend, userId, friendId);
			log.info("Дружба успешно добавлена: {} -> {}", userId, friendId);
		} catch (Exception e) {
			log.warn("Не удалось добавить дружбу {} -> {}: возможно, дубликат или нарушение ограничений", userId,
				friendId);
			throw e;
		}
	}

	@Override
	public void deleteFriend(Integer userId, Integer friendId) {
		log.debug("Попытка удалить дружбу: {} -> {}", userId, friendId);

		String deleteFriend = "DELETE FROM \"friend_list\" WHERE USER_ID = ? AND FRIEND_ID = ?";
		int rows = jdbcTemplate.update(deleteFriend, userId, friendId);

		if (rows == 0) {
			log.warn("Не удалось удалить дружбу: связь {} -> {} не найдена в таблице \"friend_list\"", userId,
				friendId);
		} else {
			log.info("Успешно удалено {} строк: {} -> {}", rows, userId, friendId);
		}
	}
}

