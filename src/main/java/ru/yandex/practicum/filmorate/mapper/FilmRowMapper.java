package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;

public class FilmRowMapper implements RowMapper<Film> {

	@Override
	public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
		return new Film(
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
	}
}
