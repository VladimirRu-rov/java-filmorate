package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

	private final JdbcTemplate jdbcTemplate;

	@Override
	public Optional<Mpa> getMpa(Integer mpaId) {
		log.debug("Поиск MPA по ID={}", mpaId);

		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM \"mpa\" WHERE id = ?", mpaId);

		if (mpaRows.next()) {
			Mpa mpa = new Mpa(mpaRows.getInt("id"), mpaRows.getString("name"));
			log.debug("MPA найден: ID={}, name='{}'", mpa.getId(), mpa.getName());
			return Optional.of(mpa);
		} else {
			log.warn("MPA с ID={} не найден в базе данных", mpaId);
			return Optional.empty();
		}
	}

	@Override
	public List<Mpa> getAllMpa() {
		log.debug("Получение всех записей MPA из базы данных");

		List<Mpa> mpaList = new ArrayList<>();
		SqlRowSet mpaRows = jdbcTemplate.queryForRowSet("SELECT * FROM \"mpa\" ORDER BY ID");

		while (mpaRows.next()) {
			Mpa mpa = new Mpa(
				mpaRows.getInt("id"),
				mpaRows.getString("name"));
			mpaList.add(mpa);
			log.trace("Добавлена запись MPA: ID={}, name='{}'", mpa.getId(), mpa.getName());
		}

		log.debug("Получено {} записей MPA", mpaList.size());
		return mpaList;
	}
}

