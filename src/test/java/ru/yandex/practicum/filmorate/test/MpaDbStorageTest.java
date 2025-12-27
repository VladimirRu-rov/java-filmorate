package ru.yandex.practicum.filmorate.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class MpaDbStorageTest {

	@Autowired
	private MpaStorage mpaStorage;

	@Test
	public void getMpaReturnExistingMpaById() {
		Optional<Mpa> result = mpaStorage.getMpa(1);

		assertThat(result).isPresent();
		Mpa mpa = result.get();
		assertThat(mpa.getId()).isEqualTo(1);
		assertThat(mpa.getName()).isEqualTo("G");
	}

	@Test
	public void getMpaWithNonExistingId() {
		Optional<Mpa> result = mpaStorage.getMpa(999);
		assertThat(result).isEmpty();
	}

	@Test
	public void getAllMpaReturnAllSixMpaRatings() {
		List<Mpa> mpaList = mpaStorage.getAllMpa();
		assertThat(mpaList).hasSize(5);
		assertThat(mpaList.get(0).getName()).isEqualTo("G");
		assertThat(mpaList.get(1).getName()).isEqualTo("PG");
		assertThat(mpaList.get(2).getName()).isEqualTo("PG-13");
		assertThat(mpaList.get(3).getName()).isEqualTo("R");
		assertThat(mpaList.get(4).getName()).isEqualTo("NC-17");
	}

	@Test
	public void getAllMpaReturnListOrderedById() {
		List<Mpa> mpaList = mpaStorage.getAllMpa();
		int previousId = 0;
		for (Mpa mpa : mpaList) {
			assertThat(mpa.getId()).isGreaterThan(previousId);
			previousId = mpa.getId();
		}
	}
}