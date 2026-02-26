package ru.practicum.explore_with_me.ewm.domain.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.EventRepo;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EventService implements ObjectValidatable {
    private final EventRepo eventRepo;

    public List<Event> findByFilter(@Nullable List<Long> users,
                                    @Nullable List<EventState> states,
                                    @Nullable List<Long> categories,
                                    @Nullable LocalDateTime rangeStart,
                                    @Nullable LocalDateTime rangeEnd,
                                    @Nullable Integer from,
                                    @Nullable Integer size) {
        return eventRepo.findByFilter(users, states, categories, rangeStart, rangeEnd, from, size);
    }

    public List<Event> findByFilter(@Nullable String text,
                                    @Nullable Boolean paid,
                                    @Nullable List<Long> categories,
                                    @Nullable LocalDateTime rangeStart,
                                    @Nullable LocalDateTime rangeEnd,
                                    @Nullable Boolean onlyAvailable,
                                    @Nullable Integer from,
                                    @Nullable Integer size,
                                    boolean sortByDate) {
        return eventRepo.findByFilter(text,
                paid,
                categories,
                rangeStart, rangeEnd,
                onlyAvailable,
                from, size, sortByDate);
    }

    public List<Event> findByInitiator(User initiator, @Nullable Integer from, @Nullable Integer size) {
        validate(initiator, "user не должен быть null");
        return eventRepo.findByUser(initiator, from, size);
    }

    public Event findByIdAndUser(Long id, User initiator) {
        validate(initiator, "initiator не должен быть null");
        validate(id, "id не должен быть null");
        Event event = eventRepo.findById(id);
        if (!event.getInitiator().getId().equals(initiator.getId())) {
            throw new NotFoundException("Такое событие не принадлежит этому пользователю");
        }
        return event;
    }

    public Event findById(Long id) {
        validate(id, "id не должен быть null");
        return eventRepo.findById(id);
    }

    public Event create(NewEvent event) {
        validate(event, "user не должен быть null");
        return eventRepo.create(event);
    }

    public Event update(Event event) {
        validate(event, "user не должен быть null");
        return eventRepo.update(event);
    }
}
