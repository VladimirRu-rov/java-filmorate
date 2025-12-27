package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

public interface MpaStorage {
	Optional<Mpa> getMpa(Integer mpaId);

	List<Mpa> getAllMpa();

}
