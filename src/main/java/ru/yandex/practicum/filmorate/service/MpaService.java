package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.interfaces.MpaStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MpaService {

	private final MpaStorage mpaStorage;

	public Mpa getMpa(Integer mpaId) {
		Optional<Mpa> mpa = mpaStorage.getMpa(mpaId);

		if (mpa.isEmpty()) {
			log.debug("Рейтинг MPA с ID {} не найден", mpaId);
			throw new NotFoundException("Рейтинг MPA с ID " + mpaId + " не найден");
		}

		log.debug("Рейтинг MPA с ID {} успешно возвращён", mpaId);
		return mpa.get();
	}

	public List<Mpa> getAllMpa() {
		List<Mpa> mpaList = mpaStorage.getAllMpa();

		if (mpaList.isEmpty()) {
			log.debug("Рейтинги MPA не найдены");
			throw new NotFoundException("Рейтинги MPA не найдены");
		}

		log.debug("Все рейтинги MPA успешно возвращены");
		return mpaList;
	}
}


