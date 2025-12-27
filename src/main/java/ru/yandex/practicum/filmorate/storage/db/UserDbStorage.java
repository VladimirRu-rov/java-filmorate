package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

	private final JdbcTemplate jdbcTemplate;
	private final UserRowMapper userRowMapper;

	@Override
	public List<User> showAllUsers() {
		log.debug("Запрос на получение всех пользователей.");
		String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM \"user\"";
		List<User> users = jdbcTemplate.query(sql, userRowMapper);

		return users.stream()
			.peek(user -> user.getFriendsId().addAll(getFriends(user.getId())))
			.collect(Collectors.toList());
	}

	@Override
	public User addUser(User user) {
		log.debug("Добавление пользователя: email={}, login={}", user.getEmail(), user.getLogin());
		String insertUser = "INSERT INTO \"user\" (EMAIL, LOGIN, NAME, BIRTHDAY) VALUES (?, ?, ?, ?)";

		jdbcTemplate.update(insertUser,
			user.getEmail(),
			user.getLogin(),
			user.getName(),
			java.sql.Date.valueOf(user.getBirthday()));

		String selectUser = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM \"user\" WHERE EMAIL = ?";
		return jdbcTemplate.queryForObject(selectUser, userRowMapper, user.getEmail());
	}

	@Override
	public Optional<User> updateUser(User user) {
		log.debug("Обновление пользователя с ID={}", user.getId());

		if (getUserById(user.getId()).isEmpty()) {
			log.warn("Пользователь с ID={} не найден для обновления.", user.getId());
			return Optional.empty();
		}

		String updateUser = "UPDATE \"user\" SET EMAIL = ?, LOGIN = ?, NAME = ?, BIRTHDAY = ? WHERE ID = ?";
		jdbcTemplate.update(updateUser,
			user.getEmail(),
			user.getLogin(),
			user.getName(),
			java.sql.Date.valueOf(user.getBirthday()),
			user.getId());

		log.info("Пользователь с ID={} успешно обновлён.", user.getId());
		return getUserById(user.getId());
	}

	@Override
	public Optional<User> getUserById(Integer userId) {
		log.debug("Поиск пользователя по ID={}", userId);
		String sql = "SELECT ID, EMAIL, LOGIN, NAME, BIRTHDAY FROM \"user\" WHERE ID = ?";
		try {
			User user = jdbcTemplate.queryForObject(sql, userRowMapper, userId);
			user.getFriendsId().addAll(getFriends(userId));
			return Optional.of(user);
		} catch (Exception e) {
			log.warn("Пользователь с ID={} не найден.", userId);
			return Optional.empty();
		}
	}

	private Set<Integer> getFriends(Integer userId) {
		log.trace("Загрузка списка друзей для пользователя ID={}", userId);
		String selectFriend = "SELECT FRIEND_ID FROM \"friend_list\" WHERE USER_ID = ?";
		return new HashSet<>(jdbcTemplate.queryForList(selectFriend, Integer.class, userId));
	}

	@Override
	public Optional<User> deleteUser(Integer userId) {
		log.info("Удаление пользователя с ID={}", userId);
		Optional<User> user = getUserById(userId);

		if (user.isEmpty()) {
			log.warn("Попытка удалить несуществующего пользователя с ID={}", userId);
			return Optional.empty();
		}

		String deleteUser = "DELETE FROM \"user\" WHERE ID = ?";
		jdbcTemplate.update(deleteUser, userId);

		log.info("Пользователь с ID={} успешно удалён.", userId);
		return user;
	}

	@Override
	public List<User> getAllFriends(Integer userId) {
		log.debug("Получение списка друзей для пользователя ID={}", userId);
		Set<Integer> friendIds = getFriends(userId);

		return friendIds.stream()
			.map(this::getUserById)
			.filter(Optional::isPresent)
			.map(Optional::get)
			.peek(friend -> log.trace("Друг добавлен в список: ID={}, login={}", friend.getId(), friend.getLogin()))
			.collect(Collectors.toList());
	}

	@Override
	public List<User> getCommonFriendsIds(Integer userId, Integer otherUserId) {
		log.debug("Поиск общих друзей для пользователей: ID1={}, ID2={}", userId, otherUserId);
		String sql = """
			SELECT u.ID, u.EMAIL, u.LOGIN, u.NAME, u.BIRTHDAY
			FROM "user" u
			INNER JOIN "friend_list" fl1 ON u.ID = fl1.FRIEND_ID
			INNER JOIN "friend_list" fl2 ON u.ID = fl2.FRIEND_ID
			WHERE fl1.USER_ID = ? AND fl2.USER_ID = ?
			""";

		return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
	}
}