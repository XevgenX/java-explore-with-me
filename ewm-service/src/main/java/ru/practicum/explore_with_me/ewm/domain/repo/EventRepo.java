package ru.practicum.explore_with_me.ewm.domain.repo;

import jakarta.annotation.Nullable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepo {
    List<Event> findByFilter(@Nullable List<Long> users,
                             @Nullable List<EventState> states,
                             @Nullable List<Long> categories,
                             @Nullable LocalDateTime rangeStart,
                             @Nullable LocalDateTime rangeEnd,
                             @Nullable Integer from,
                             @Nullable Integer size);

    List<Event> findByUser(User initiator, @Nullable Integer from, @Nullable Integer size);

    Event findById(Long id);

    Event create(NewEvent event);

    Event update(Event event);

    List<Event> findByFilter(@Nullable String text, @Nullable Boolean paid,
                             @Nullable List<Long> categories,
                             @Nullable LocalDateTime rangeStart, @Nullable LocalDateTime rangeEnd,
                             @Nullable Boolean onlyAvailable,
                             @Nullable Integer from, @Nullable Integer size,
                             boolean sortByDate);
}
