package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {

	private Long id;

	@NotBlank(message = "Электронная почта не может быть пустой")
	@Email(message = "Email должен быть в корректном формате (содержать @)")
	private String email;

	@NotBlank(message = "Логин не может быть пустым")
	@Pattern(regexp = "^\\S+$", message = "Логин не должен содержать пробелы")
	private String login;

	private String name;

	@NotNull(message = "Дата рождения обязательна")
	@Past(message = "Дата рождения не может быть в будущем")
	private LocalDate birthday;

	private Set<Long> friends = new HashSet<>();

}

