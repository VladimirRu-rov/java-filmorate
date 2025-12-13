package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

	private final JdbcTemplate jdbcTemplate;
	private final GenreRowMapper genreRowMapper;

	@Override
	public Optional<Genre> getGenre(Integer genreId) {
		log.debug("Поиск жанра по ID={}", genreId);
		String sql = "SELECT * FROM \"genres\" WHERE ID = ?";
		List<Genre> result = jdbcTemplate.query(sql, genreRowMapper, genreId);
		if (!result.isEmpty()) {
			Genre genre = result.get(0);
			log.debug("Жанр найден: ID={}, name='{}'", genre.getId(), genre.getName());
			return Optional.of(genre);
		}
		log.warn("Жанр с ID={} не найден", genreId);
		return Optional.empty();
	}

	@Override
	public List<Genre> getAllGenres() {
		log.debug("Получение всех жанров из базы данных");
		String sql = "SELECT * FROM \"genres\" ORDER BY ID";
		List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper);
		log.debug("Загружено {} жанров", genres.size());
		return genres;
	}
}
