package ru.practicum.explore_with_me.ewm.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventFullDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventShortDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.LocationDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.NewEventDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventAdminDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventUserDto;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewLocation;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class EventApiMapper {
    private final CategoryApiMapper categoryMapper;
    private final UserApiMapper userMapper;
    private final StateApiMapper stateApiMapper;

    public EventShortDto toShortDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventShortDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests().longValue() : 0L)
                .eventDate(event.getEventDate())
                .initiator(userMapper.toDto(event.getInitiator()))
                .paid(event.getPaid())
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews().longValue() : 0L)
                .build();
    }

    public List<EventShortDto> toShortDto(List<Event> events) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .map(this::toShortDto)
                .collect(Collectors.toList());
    }

    public EventFullDto toFullDto(Event event) {
        if (event == null) {
            return null;
        }

        return EventFullDto.builder()
                .id(event.getId())
                .annotation(event.getAnnotation())
                .category(categoryMapper.toDto(event.getCategory()))
                .confirmedRequests(event.getConfirmedRequests() != null ? event.getConfirmedRequests().longValue() : 0L)
                .createdOn(event.getCreatedOn())
                .description(event.getDescription())
                .eventDate(event.getEventDate())
                .initiator(userMapper.toDto(event.getInitiator()))
                .location(toLocationDto(event.getLocation()))
                .paid(event.getPaid())
                .participantLimit(event.getParticipantLimit())
                .publishedOn(event.getPublishedOn())
                .requestModeration(event.getRequestModeration())
                .state(stateApiMapper.mapEventState(event.getState()))
                .title(event.getTitle())
                .views(event.getViews() != null ? event.getViews().longValue() : 0L)
                .build();
    }

    public List<EventFullDto> toFullDto(List<Event> events) {
        if (events == null) {
            return List.of();
        }
        return events.stream()
                .map(this::toFullDto)
                .collect(Collectors.toList());
    }

    public NewEvent toNewEvent(NewEventDto request, User initiator, Category category) {
        if (request == null) {
            return null;
        }

        return NewEvent.builder()
                .initiator(initiator)
                .category(category)
                .title(request.getTitle())
                .annotation(request.getAnnotation())
                .description(request.getDescription())
                .eventDate(request.getEventDate())
                .paid(request.getPaid())
                .participantLimit(request.getParticipantLimit())
                .requestModeration(request.getRequestModeration())
                .location(NewLocation.builder()
                        .lat(request.getLocation().getLat())
                        .lon(request.getLocation().getLon())
                        .build())
                .build();
    }

    public Event updateEventFromUserRequest(Event event, UpdateEventUserDto request,
                                            Category category) {
        if (event == null || request == null) {
            return event;
        }
        Location location = null;
        if (Objects.nonNull(request.getLocation()) && (!event.getLocation().getLat().equals(request.getLocation().getLat())
                || !event.getLocation().getLon().equals(request.getLocation().getLon()))) {
            location = Location.builder()
                    .lat(request.getLocation().getLat())
                    .lon(request.getLocation().getLon())
                    .build();
        }

        Event.EventBuilder builder = Event.builder()
                .id(event.getId())
                .initiator(event.getInitiator())
                .category(category != null ? category : event.getCategory())
                .createdOn(event.getCreatedOn())
                .title(Objects.nonNull(request.getTitle()) ? request.getTitle() : event.getTitle())
                .annotation(Objects.nonNull(request.getAnnotation()) ? request.getAnnotation() : event.getAnnotation())
                .description(Objects.nonNull(request.getDescription()) ? request.getDescription() : event.getDescription())
                .eventDate(Objects.nonNull(request.getEventDate()) ? request.getEventDate() : event.getEventDate())
                .paid(Objects.nonNull(request.getPaid()) ? request.getPaid() : event.getPaid())
                .participantLimit(Objects.nonNull(request.getParticipantLimit()) ? request.getParticipantLimit() : event.getParticipantLimit())
                .requestModeration(Objects.nonNull(request.getRequestModeration()) ? request.getRequestModeration() : event.getRequestModeration())
                .location(location != null ? location : event.getLocation())
                .views(event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .state(stateApiMapper.updateStateFromUserAction(event.getState(), request.getStateAction()));

        return builder.build();
    }

    public Event updateEventFromAdminRequest(Event event, UpdateEventAdminDto request,
                                             Category category) {
        if (event == null || request == null) {
            throw new ForbiddenException("Нельзя изменить опубликованное событие");
        }

        if (event.getState().equals(EventState.PUBLISHED)
                || event.getState().equals(EventState.CANCEL_REVIEW)
                || event.getState().equals(EventState.PUBLICATION_CANCELED)) {
            throw new ForbiddenException("Нельзя изменить опубликованное событие");
        }

        Location location = null;
        if (Objects.nonNull(request.getLocation()) && (!event.getLocation().getLat().equals(request.getLocation().getLat())
                || !event.getLocation().getLon().equals(request.getLocation().getLon()))) {
            location = Location.builder()
                    .lat(request.getLocation().getLat())
                    .lon(request.getLocation().getLon())
                    .build();
        }

        Event.EventBuilder builder = Event.builder()
                .id(event.getId())
                .initiator(event.getInitiator())
                .category(category != null ? category : event.getCategory())
                .createdOn(event.getCreatedOn())
                .title(Objects.nonNull(request.getTitle()) ? request.getTitle() : event.getTitle())
                .annotation(Objects.nonNull(request.getAnnotation()) ? request.getAnnotation() : event.getAnnotation())
                .description(Objects.nonNull(request.getDescription()) ? request.getDescription() : event.getDescription())
                .eventDate(Objects.nonNull(request.getEventDate()) ? request.getEventDate() : event.getEventDate())
                .paid(Objects.nonNull(request.getPaid()) ? request.getPaid() : event.getPaid())
                .participantLimit(Objects.nonNull(request.getParticipantLimit()) ? request.getParticipantLimit() : event.getParticipantLimit())
                .requestModeration(Objects.nonNull(request.getRequestModeration()) ? request.getRequestModeration() : event.getRequestModeration())
                .location(location != null ? location : event.getLocation())
                .views(event.getViews())
                .confirmedRequests(event.getConfirmedRequests())
                .publishedOn(event.getPublishedOn())
                .state(stateApiMapper.updateStateFromAdminAction(event.getState(), request.getStateAction()));

        if (request.getStateAction() == UpdateEventAdminDto.AdminStateAction.PUBLISH_EVENT) {
            builder.publishedOn(LocalDateTime.now());
        }

        return builder.build();
    }

    private LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        return LocationDto.builder()
                .lat(location.getLat().floatValue())
                .lon(location.getLon().floatValue())
                .build();
    }
}
