package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
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
public class User {

	private Integer id;

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

	private Set<Integer> friendsId = new HashSet<>();

	public User(Integer id, String email, String login, String name, LocalDate birthday) {
		this.id = id;
		this.email = email;
		this.login = login;
		this.name = name;
		this.birthday = birthday;
	}
}

