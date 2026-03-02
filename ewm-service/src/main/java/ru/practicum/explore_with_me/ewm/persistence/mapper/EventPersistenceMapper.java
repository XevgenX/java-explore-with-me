package ru.practicum.explore_with_me.ewm.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.repo.StatRepo;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EventPersistenceMapper {
    private final UserPersistenceMapper userMapper;
    private final CategoryPersistenceMapper categoryMapper;
    private final LocationPersistenceMapper locationMapper;
    private final StatRepo statRepo;

    public Event toDomain(EventEntity entity) {
        if (entity == null) {
            return null;
        }

        return Event.builder()
                .id(entity.getId())
                .initiator(userMapper.toDomain(entity.getInitiator()))
                .category(categoryMapper.toDomain(entity.getCategory()))
                .createdOn(entity.getCreatedOn())
                .title(entity.getTitle())
                .annotation(entity.getAnnotation())
                .description(entity.getDescription())
                .eventDate(entity.getEventDate())
                .paid(entity.getPaid())
                .participantLimit(entity.getParticipantLimit())
                .requestModeration(entity.getRequestModeration())
                .location(locationMapper.toDomain(entity.getLocation()))
                .views(statRepo.getViewsForEvent(entity.getId(), entity.getCreatedOn()))
                .confirmedRequests(entity.getConfirmedRequests())
                .publishedOn(entity.getPublishedOn())
                .state(entity.getState())
                .build();
    }

    public List<Event> toDomain(List<EventEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public List<EventEntity> toEntities(List<Event> domain) {
        return domain.stream().map(this::toEntity).collect(Collectors.toList());
    }

    public EventEntity toEntity(Event domain) {
        if (domain == null) {
            return null;
        }

        return EventEntity.builder()
                .id(domain.getId())
                .initiator(userMapper.toEntity(domain.getInitiator()))
                .category(categoryMapper.toEntity(domain.getCategory()))
                .createdOn(domain.getCreatedOn())
                .title(domain.getTitle())
                .annotation(domain.getAnnotation())
                .description(domain.getDescription())
                .eventDate(domain.getEventDate())
                .paid(domain.getPaid())
                .participantLimit(domain.getParticipantLimit())
                .requestModeration(domain.getRequestModeration())
                .location(locationMapper.toEntity(domain.getLocation()))
                .confirmedRequests(domain.getConfirmedRequests())
                .publishedOn(domain.getPublishedOn())
                .state(domain.getState())
                .build();
    }

    public void updateEntityFromDomain(Event domain, EventEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        if (Objects.nonNull(domain.getTitle()) && !domain.getTitle().isBlank()) {
            entity.setTitle(domain.getTitle());
        }
        if (Objects.nonNull(domain.getAnnotation()) && !domain.getAnnotation().isBlank()) {
            entity.setAnnotation(domain.getAnnotation());
        }
        if (Objects.nonNull(domain.getDescription()) && !domain.getDescription().isBlank()) {
            entity.setDescription(domain.getDescription());
        }
        if (Objects.nonNull(domain.getEventDate())) {
            entity.setEventDate(domain.getEventDate());
        }
        if (Objects.nonNull(domain.getPaid())) {
            entity.setPaid(domain.getPaid());
        }
        if (Objects.nonNull(domain.getParticipantLimit())) {
            entity.setParticipantLimit(domain.getParticipantLimit());
        }
        if (Objects.nonNull(domain.getRequestModeration())) {
            entity.setRequestModeration(domain.getRequestModeration());
        }
        if (Objects.nonNull(domain.getLocation())) {
            locationMapper.updateEntityFromDomain(domain.getLocation(), entity.getLocation());
        }
        if (Objects.nonNull(domain.getConfirmedRequests())) {
            entity.setConfirmedRequests(domain.getConfirmedRequests());
        }
        if (Objects.nonNull(domain.getPublishedOn())) {
            entity.setPublishedOn(domain.getPublishedOn());
        }
        if (Objects.nonNull(domain.getState())) {
            entity.setState(domain.getState());
        }
    }

    public EventEntity toNewEntity(NewEvent domain) {
        if (domain == null) {
            return null;
        }

        return EventEntity.builder()
                .initiator(userMapper.toEntity(domain.getInitiator()))
                .category(categoryMapper.toEntity(domain.getCategory()))
                .createdOn(LocalDateTime.now())
                .title(domain.getTitle())
                .annotation(domain.getAnnotation())
                .description(domain.getDescription())
                .eventDate(domain.getEventDate())
                .paid(domain.getPaid() != null ? domain.getPaid() : false)
                .participantLimit(domain.getParticipantLimit() != null ? domain.getParticipantLimit() : 0)
                .requestModeration(domain.getRequestModeration() != null ? domain.getRequestModeration() : true)
                .location(locationMapper.toNewEntity(domain.getLocation()))
                .confirmedRequests(0)
                .publishedOn(LocalDateTime.now())
                .state(EventState.WAITING_FOR_PUBLICATION)
                .build();
    }
}
