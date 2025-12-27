package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
public class Film {

	private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
	private Set<Long> likes = new HashSet<>();
	private Integer id;
	@NotBlank(message = "Название не может быть пустым.")
	private String name;
	@Size(max = 200, message = "Описание не может быть длиннее 200 символов.")
	private String description;
	@NotNull(message = "Дата релиза должна быть обязательно")
	private LocalDate releaseDate;
	@Min(value = 1, message = "Продолжительность фильма не может быть отрицательной или нулевой.")
	private Integer duration;
	private Mpa mpa;
	private Set<Genre> genres = new HashSet<>();

	public Film(Integer id, String name, String description, LocalDate releaseDate, Integer duration, Mpa mpa) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.releaseDate = releaseDate;
		this.duration = duration;
		this.mpa = mpa;
	}

	public static LocalDate getMinDate() {
		return MIN_DATE;
	}
}


