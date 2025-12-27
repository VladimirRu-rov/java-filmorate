package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.GenreStorage;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

	private final JdbcTemplate jdbcTemplate;
	private final GenreStorage genreStorage;
	private final MpaStorage mpaStorage;

	private final ResultSetExtractor<List<Film>> filmsExtractor = rs -> {
		Map<Integer, Film> filmMap = new LinkedHashMap<>();
		Film film;

		while (rs.next()) {
			Integer filmId = rs.getInt("id");
			film = filmMap.get(filmId);

			if (film == null) {
				film = new Film(
					rs.getInt("id"),
					rs.getString("name"),
					rs.getString("description"),
					rs.getDate("release_date").toLocalDate(),
					rs.getInt("duration"),
					new Mpa(
						rs.getInt("m_id"),
						rs.getString("m_name")
					)
				);
				filmMap.put(filmId, film);
			}

			Set<Genre> genres = film.getGenres();
			if (rs.getInt("genre_id") != 0) {
				genres.add(new Genre(
					rs.getInt("genre_id"),
					rs.getString("g_name")
				));
			}

			Set<Long> userLike = film.getLikes();
			if (rs.getInt("user_id") != 0) {
				userLike.add(rs.getLong("user_id"));
			}
		}

		log.trace("Извлечено {} фильмов с жанрами и лайками", filmMap.size());
		return new ArrayList<>(filmMap.values());
	};

	@Override
	public Film addFilm(Film film) {
		validateFilm(film);
		log.info("Добавление фильма: '{}'", film.getName());

		String insertFilm = """
			INSERT INTO "film" (NAME, DESCRIPTION, MPA_ID, RELEASE_DATE, DURATION)
			VALUES (?, ?, ?, ?, ?)
			""";

		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(insertFilm, new String[] {"id"});
			ps.setString(1, film.getName());
			ps.setString(2, film.getDescription());
			ps.setInt(3, film.getMpa().getId());
			ps.setDate(4, java.sql.Date.valueOf(film.getReleaseDate()));
			ps.setInt(5, film.getDuration());
			return ps;
		}, keyHolder);

		int filmId = keyHolder.getKey().intValue();
		film.setId(filmId);

		if (!film.getGenres().isEmpty()) {
			addGenresToFilm(filmId, film.getGenres());
			film.setGenres(getFilmGenres(filmId));
		}

		log.debug("Фильм успешно добавлен с ID={}", filmId);
		return film;
	}

	@Override
	public Optional<Film> updateFilm(Film film) {
		validateFilm(film);
		if (getFilmById(film.getId()).isEmpty()) {
			log.warn("Фильм с ID={} не найден при обновлении", film.getId());
			return Optional.empty();
		}

		log.info("Обновление фильма с ID={}: '{}'", film.getId(), film.getName());
		String updateFilm =
			"UPDATE \"film\" SET NAME = ?, RELEASE_DATE = ?, DESCRIPTION = ?, DURATION = ?, MPA_ID = ? WHERE ID = ?";
		jdbcTemplate.update(updateFilm,
			film.getName(),
			film.getReleaseDate(),
			film.getDescription(),
			film.getDuration(),
			film.getMpa().getId(),
			film.getId());

		String deleteGenre = "DELETE FROM \"film_genre\" WHERE FILM_ID = ?";
		jdbcTemplate.update(deleteGenre, film.getId());

		if (!film.getGenres().isEmpty()) {
			addGenresToFilm(film.getId(), film.getGenres());
		}

		log.debug("Фильм с ID={} успешно обновлён", film.getId());
		return getFilmById(film.getId());
	}

	@Override
	public List<Film> showAllFilm() {
		log.debug("Получение списка всех фильмов");

		String filmSql = """
			SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
			       m.ID AS m_id, m.NAME AS m_name
			FROM "film" f
			LEFT JOIN "mpa" m ON f.MPA_ID = m.ID
			""";

		List<Film> films = jdbcTemplate.query(filmSql, new FilmRowMapper());

		for (Film film : films) {
			film.setGenres(getFilmGenres(film.getId()));
			addLikesToFilm(film);
		}

		log.debug("Найдено {} фильмов", films.size());
		return films;
	}

	@Override
	public Optional<Film> deleteFilm(Integer filmId) {
		Optional<Film> film = getFilmById(filmId);
		if (film.isEmpty()) {
			log.warn("Фильм с ID={} не найден при удалении", filmId);
			return Optional.empty();
		}

		log.info("Удаление фильма с ID={}", filmId);
		jdbcTemplate.update("DELETE FROM \"film_genre\" WHERE \"FILM_ID\" = ?", filmId);
		jdbcTemplate.update("DELETE FROM \"film\" WHERE \"ID\" = ?", filmId);

		log.debug("Фильм с ID={} успешно удалён", filmId);
		return film;
	}

	private void addLikesToFilm(Film film) {
		log.debug("Добавление лайков к фильму с ID={}", film.getId());
		SqlRowSet likeRows = jdbcTemplate.queryForRowSet("SELECT * FROM \"like_list\" WHERE FILM_ID = ?", film.getId());

		while (likeRows.next()) {
			film.getLikes().add(likeRows.getLong("user_id"));
		}
	}

	@Override
	public List<Film> getMostPopularFilm(Integer count) {
		if (count <= 0) {
			log.debug("Запрос топ-{} фильмов: количество <= 0, возвращаем пустой список", count);
			return Collections.emptyList();
		}

		log.debug("Получение топ-{} самых популярных фильмов", count);
		String sql = """
			SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION,
			       m.ID AS m_id, m.NAME AS m_name, COUNT(l.USER_ID) AS likes_count
			FROM "film" f
			LEFT JOIN "like_list" l ON f.ID = l.FILM_ID
			LEFT JOIN "mpa" m ON f.MPA_ID = m.ID
			GROUP BY f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE, f.DURATION, m.ID, m.NAME
			ORDER BY likes_count DESC, f.ID DESC
			LIMIT ?
			""";

		return jdbcTemplate.query(sql, new FilmRowMapper(), count);
	}

	@Override
	public Optional<Film> getFilmById(Integer id) {
		try {
			log.debug("Получение фильма по ID={}", id);

			String sqlRequest = """
				SELECT f.ID, f.NAME, f.DESCRIPTION, f.RELEASE_DATE,
				       f.DURATION, f.MPA_ID, m.ID m_id, m.NAME m_name
				FROM "film" f
				LEFT JOIN "mpa" m ON f.MPA_ID = m.ID
				WHERE f.ID = ?
				""";

			Film film = jdbcTemplate.queryForObject(sqlRequest, new FilmRowMapper(), id);

			film.setGenres(getFilmGenres(id));
			addLikesToFilm(film);

			log.debug("Фильм с ID={} успешно получен", id);
			return Optional.of(film);
		} catch (EmptyResultDataAccessException e) {
			log.warn("Фильм с ID={} не найден", id);
			return Optional.empty();
		} catch (DataAccessException e) {
			log.error("Ошибка доступа к данным при получении фильма с ID={}: {}", id, e.getMessage(), e);
			throw e;
		}
	}

	private Set<Genre> getFilmGenres(int filmId) {
		log.debug("Получение жанров для фильма с ID={}", filmId);
		Set<Genre> genres = new LinkedHashSet<>();
		SqlRowSet filmGenreRows = jdbcTemplate.queryForRowSet(
			"SELECT * FROM \"film_genre\" WHERE FILM_ID = ? ORDER BY GENRE_ID", filmId);

		while (filmGenreRows.next()) {
			int genreId = filmGenreRows.getInt("genre_id");
			Optional<Genre> genre = genreStorage.getGenre(genreId);
			if (genre.isPresent()) {
				genres.add(genre.get());
			} else {
				log.warn("Жанр с ID={} не найден для фильма ID={}", genreId, filmId);
			}
		}
		log.debug("Для фильма с ID={} найдено {} жанров", filmId, genres.size());
		return genres;
	}

	private void addGenresToFilm(Integer filmId, Set<Genre> genres) {
		log.debug("Добавление {} жанров к фильму ID={}", genres.size(), filmId);
		String insertSql = "INSERT INTO \"film_genre\" (FILM_ID, GENRE_ID) VALUES (?, ?)";
		List<Object[]> batchArgs = new ArrayList<>();

		for (Genre genre : genres) {
			log.trace("Подготовка жанра ID={} для фильма ID={}", genre.getId(), filmId);
			batchArgs.add(new Object[] {filmId, genre.getId()});
		}

		try {
			jdbcTemplate.batchUpdate(insertSql, batchArgs);
			log.debug("Успешно добавлено {} записей в таблицу film_genre", batchArgs.size());
		} catch (DataAccessException e) {
			log.error("Ошибка при пакетной вставке жанров для фильма ID={}: {}", filmId, e.getMessage(), e);
			throw new NotFoundException("Жанр с ID=" + " не найден");
		}
	}

	private void validateFilm(Film film) {
		log.debug("Валидация фильма: '{}'", film.getName());

		if (film.getReleaseDate().isBefore(Film.getMinDate())) {
			log.warn("Дата релиза фильма '{}' слишком ранняя: {}", film.getName(), film.getReleaseDate());
			throw new ValidationException("Дата релиза слишком ранняя. Минимум: " + Film.getMinDate());
		}
		log.info("Валидация MPA: переданный mpa.id = {}", film.getMpa() != null ? film.getMpa().getId() : "null");

		Optional<Mpa> mpa = mpaStorage.getMpa(film.getMpa().getId());
		if (mpa.isEmpty()) {
			log.warn("MPA с ID={} не найден для фильма '{}'", film.getMpa().getId(), film.getName());
			throw new NotFoundException("MPA с ID=" + film.getMpa().getId() + " не найден");
		}

		log.debug("MPA с ID={} успешно найден для фильма '{}'", film.getMpa().getId(), film.getName());

		if (film.getGenres() != null) {
			Set<Integer> genreIds = film.getGenres().stream()
				.map(Genre::getId)
				.collect(Collectors.toSet());

			if (genreIds.size() != film.getGenres().size()) {
				log.warn("Обнаружены дублирующиеся ID жанров у фильма '{}'", film.getName());
				throw new ValidationException("Дублирующийся ID жанра");
			}

			for (Integer genreId : genreIds) {
				Optional<Genre> existingGenre = genreStorage.getGenre(genreId);
				if (existingGenre.isEmpty()) {
					log.warn("Жанр с ID={} не найден для фильма '{}'", genreId, film.getName());
					throw new NotFoundException("Жанр с ID=" + genreId + " не найден");
				}
			}
			log.debug("Все жанры фильма '{}' прошли валидацию", film.getName());
		} else {
			log.debug("Поле genres не указано для фильма '{}'", film.getName());
		}
	}
}



