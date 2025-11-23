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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerValidationTest {

	@Autowired
	private MockMvc mockMvc;

	private User user;
	private ObjectMapper objectMapper;

	@BeforeEach
	public void setUp() {
		user = new User();
		user.setEmail("user@example.com");
		user.setLogin("validlogin");
		user.setName("John");
		user.setBirthday(LocalDate.of(1990, 1, 1));

		objectMapper = new ObjectMapper();
		objectMapper.registerModule(new JavaTimeModule());
		objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Test
	public void whenEmailIsNull() throws Exception {
		user.setEmail(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenEmailIsEmpty() throws Exception {
		user.setEmail("");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenEmailMissingAtSymbol() throws Exception {
		user.setEmail("invalid-email");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenEmailIsValidWithSubdomain() throws Exception {
		user.setEmail("user@sub.example.com");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk());
	}

	@Test
	public void whenLoginIsNull() throws Exception {
		user.setLogin(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenLoginContainsSpace() throws Exception {
		user.setLogin("bad login");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenLoginIsValidWithSpecialChars() throws Exception {
		user.setLogin("user_123");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk());
	}

	@Test
	public void setNameFromLoginWhenNameIsNull() throws Exception {
		user.setName(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk())
			.andExpect(mvcResult -> {
				User savedUser = objectMapper.readValue(
					mvcResult.getResponse().getContentAsString(), User.class);
				assert savedUser.getName().equals(user.getLogin());
			});
	}

	@Test
	public void setNameFromLoginWhenNameIsWhitespace() throws Exception {
		user.setName("   ");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk())
			.andExpect(mvcResult -> {
				User savedUser = objectMapper.readValue(
					mvcResult.getResponse().getContentAsString(), User.class);
				assert savedUser.getName().equals(user.getLogin());
			});
	}

	@Test
	public void nameDoesNotChangeIfAlreadySet() throws Exception {
		String originalName = "John";
		user.setName(originalName);
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk())
			.andExpect(mvcResult -> {
				User savedUser = objectMapper.readValue(
					mvcResult.getResponse().getContentAsString(), User.class);
				assert savedUser.getName().equals(originalName);
			});
	}

	@Test
	public void whenBirthdayIsNull() throws Exception {
		user.setBirthday(null);
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenBirthdayInFuture() throws Exception {
		user.setBirthday(LocalDate.now().plusDays(1));
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void rejectTodayAsBirthday() throws Exception {
		user.setBirthday(LocalDate.now());

		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isBadRequest());
	}

	@Test
	public void whenLoginOnlySpecialChars() throws Exception {
		user.setLogin("!_*");
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk());
	}

	@Test
	public void validUserCreation() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.post("/users")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(user)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.id").isNumber())
			.andExpect(jsonPath("$.email").value(user.getEmail()))
			.andExpect(jsonPath("$.login").value(user.getLogin()))
			.andExpect(jsonPath("$.name").value(user.getName()))
			.andExpect(jsonPath("$.birthday").value(user.getBirthday().toString()));
	}
}

