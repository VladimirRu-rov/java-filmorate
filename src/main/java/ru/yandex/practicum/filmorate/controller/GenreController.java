package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@Slf4j
public class GenreController {
	private final GenreService genreService;

	@GetMapping("/{genreId}")
	public Genre getGenre(@PathVariable Integer genreId) {
		return genreService.getGenre(genreId);
	}

	@GetMapping
	public Collection<Genre> getAllGenres() {
		return genreService.getAllGenres();
	}

}
