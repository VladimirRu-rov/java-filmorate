package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendsStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FriendsDbStorageTest {

	@Autowired
	private FriendsStorage friendsStorage;

	@Autowired
	private UserStorage userStorage;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private User user1;
	private User user2;

	@BeforeEach
	public void setUp() {
		user1 = User.builder().email("user1@example.com").login("user1login").name("User One")
			.birthday(LocalDate.of(1990, 1, 1)).build();

		user2 = User.builder().email("user2@example.com").login("user2login").name("User Two")
			.birthday(LocalDate.of(1995, 5, 5)).build();

		user1 = userStorage.addUser(user1);
		user2 = userStorage.addUser(user2);
	}

	@Test
	public void shouldEstablishFriendship() {
		friendsStorage.addFriends(user1.getId(), user2.getId());

		Integer count =
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"friend_list\" WHERE USER_ID = ? AND FRIEND_ID = ?",
				Integer.class, user1.getId(), user2.getId());
		assertThat(count).isEqualTo(1);
	}

	@Test
	public void shouldRemoveFriendship() {
		friendsStorage.addFriends(user1.getId(), user2.getId());
		friendsStorage.deleteFriend(user1.getId(), user2.getId());

		Integer count =
			jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"friend_list\" WHERE USER_ID = ? AND FRIEND_ID = ?",
				Integer.class, user1.getId(), user2.getId());
		assertThat(count).isZero();
	}
}