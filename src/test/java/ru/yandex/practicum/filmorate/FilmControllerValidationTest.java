package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.model.Film;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmControllerValidationTest {

	@Autowired
	private MockMvc mockMvc;

	private Film film;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() {
		film = new Film();
		film.setName("Valid Title");
		film.setDescription("Valid description");
		film.setReleaseDate(Film.getMinDate().plusDays(1));
		film.setDuration(90L);

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Test
	public void whenNameIsNull() throws Exception {
		film.setName(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenNameIsEmpty() throws Exception {
		film.setName("");
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenDescriptionTooLong() throws Exception {
		film.setDescription("a".repeat(201));
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenReleaseDateIsNull() throws Exception {
		film.setReleaseDate(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenReleaseDateBeforeMin() throws Exception {
		film.setReleaseDate(Film.getMinDate().minusDays(1));
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void minReleaseDate() throws Exception {
		film.setReleaseDate(Film.getMinDate());
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isOk());
	}

	@Test
	public void whenDurationIsZero() throws Exception {
		film.setDuration(0L);
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenDurationIsNegative() throws Exception {
		film.setDuration(-10L);
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenDurationIsValid() throws Exception {
		film.setDuration(1L);
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isOk());
	}

	@Test
	public void shouldPassValidFilm() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isOk());
	}

	@Test
	public void validFilmCreation() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/films")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(film)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.name").value(film.getName()))
			.andExpect(jsonPath("$.description").value(film.getDescription()))
			.andExpect(jsonPath("$.releaseDate").value(film.getReleaseDate().toString()))
			.andExpect(jsonPath("$.duration").value(film.getDuration()));
	}
}

