package ru.practicum.explore_with_me.ewm.domain.repo;

import java.time.LocalDateTime;

public interface StatRepo {
    Long getViewsForEvent(Long eventId, LocalDateTime created);
}
