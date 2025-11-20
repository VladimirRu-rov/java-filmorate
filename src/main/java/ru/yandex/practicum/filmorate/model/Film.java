package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {

	private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
	private Long id;
	private String name;
	private String description;
	private LocalDate releaseDate;
	private long duration;

	public static LocalDate getMinDate() {
		return MIN_DATE;
	}
}


