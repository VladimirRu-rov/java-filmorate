package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {

	private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

	private Long id;

	@NotBlank(message = "Название не может быть пустым.")
	private String name;

	@Size(max = 200, message = "Описание не может быть длиннее 200 символов.")
	private String description;

	@NotNull(message = "Дата релиза должна быть обязательно")
	private LocalDate releaseDate;

	@Min(value = 1, message = "Продолжительность фильма не может быть отрицательной или нулевой.")
	private long duration;

	public static LocalDate getMinDate() {
		return MIN_DATE;
	}
}


