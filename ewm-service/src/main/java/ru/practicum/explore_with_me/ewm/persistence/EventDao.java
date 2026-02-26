package ru.practicum.explore_with_me.ewm.persistence;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.EventRepo;
import ru.practicum.explore_with_me.ewm.persistence.mapper.EventPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
@Repository
public class EventDao implements EventRepo {
    private final EventRepository repository;
    private final EventPersistenceMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<Event> findByFilter(@Nullable List<Long> users,
                                    @Nullable List<EventState> states,
                                    @Nullable List<Long> categories,
                                    @Nullable LocalDateTime rangeStart,
                                    @Nullable LocalDateTime rangeEnd,
                                    Integer from,
                                    Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return mapper.toDomain(repository.findByFilter(users, states, categories, rangeStart, rangeEnd, page));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Event> findByUser(User initiator, Integer from, Integer size) {
        Pageable page = null;
        if (Objects.nonNull(from) && Objects.nonNull(size)) {
            page = PageRequest.of(from / size, size);
        } else if (Objects.isNull(from) && Objects.nonNull(size)) {
            page = PageRequest.ofSize(size);
        }
        return mapper.toDomain(repository.findAll(page));
    }

    @Transactional(readOnly = true)
    @Override
    public Event findById(Long id) {
        return mapper.toDomain(repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Такого события не существует")));
    }

    @Transactional
    @Override
    public Event create(NewEvent event) {
        return mapper.toDomain(repository.save(mapper.toNewEntity(event)));
    }

    @Transactional
    @Override
    public Event update(Event event) {
        return mapper.toDomain(repository.save(mapper.toEntity(event)));
    }

    @Transactional(readOnly = true)
    @Override
    public List<Event> findByFilter(@Nullable String text, @Nullable Boolean paid, @Nullable List<Long> categories, @Nullable LocalDateTime rangeStart, @Nullable LocalDateTime rangeEnd, @Nullable Boolean onlyAvailable, Integer from, Integer size, boolean sortByDate) {
        Sort sort = sortByDate ?
                Sort.by(Sort.Direction.ASC, "eventDate") :
                Sort.by(Sort.Direction.DESC, "views");
        Pageable page = PageRequest.of(from / size, size, sort);
        return mapper.toDomain(repository.findByFilter(text,
                paid,
                categories,
                rangeStart, rangeEnd,
                onlyAvailable,
                page));
    }
}
