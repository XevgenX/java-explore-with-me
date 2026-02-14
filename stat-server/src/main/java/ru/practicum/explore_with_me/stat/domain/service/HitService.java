package ru.practicum.explore_with_me.stat.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;
import ru.practicum.explore_with_me.stat.domain.repo.HitRepo;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HitService implements ObjectValidatable {
    private final HitRepo repo;

    public Hit create(NewHit hit) {
        validate(hit, "Данные о посещении не корректны");
        return repo.create(hit);
    }

    public List<Hit> getAll() {
        return repo.findAll();
    }

    public List<HitsStat> getByFilter(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean uniq) {
        validate(start, "Дата начала фильтрации не корректна");
        validate(end, "Дата окончания фильтрации не корректна");
        if (start.isAfter(end)) {
            throw new ValidationException("Дата начала не должна быть после даты окончания");
        }
        return repo.findByFilter(start, end, uris, uniq);
    }
}
