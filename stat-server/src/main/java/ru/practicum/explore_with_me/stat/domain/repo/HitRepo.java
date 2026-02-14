package ru.practicum.explore_with_me.stat.domain.repo;

import org.springframework.lang.Nullable;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;

import java.time.LocalDateTime;
import java.util.List;

public interface HitRepo {
    Hit create(NewHit hit);

    List<Hit> findAll();

    List<HitsStat> findByFilter(LocalDateTime start, LocalDateTime end,
                                @Nullable List<String> uris,
                                @Nullable Boolean uniq);
}
