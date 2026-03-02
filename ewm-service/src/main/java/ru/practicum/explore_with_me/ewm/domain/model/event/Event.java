package ru.practicum.explore_with_me.ewm.domain.model.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.DateInFutureValidatable;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class Event implements IdValidatable, ObjectValidatable, DateInFutureValidatable, TextValidatable {
    private final Long id;
    private final User initiator;
    private final Category category;
    private final LocalDateTime createdOn;
    private String title;
    private String annotation;
    private String description;
    private LocalDateTime eventDate;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;
    private final Location location;
    private Long views;
    private Integer confirmedRequests;
    private LocalDateTime publishedOn;
    private EventState state;

    public Event(Long id, User initiator, Category category,
                 LocalDateTime createdOn,
                 String title, String annotation, String description, LocalDateTime eventDate,
                 Boolean paid, Integer participantLimit, Boolean requestModeration,
                 Location location, Long views, Integer confirmedRequests,
                 LocalDateTime publishedOn, EventState state) {
        validate(id, "У категории должен корректным быть id");
        validate(initiator, "У события должно быть корректным initiator");
        validate(category, "У события должно быть корректным category");
        validate(annotation, "У события должно быть корректным annotation");
        validate(description, "У события должно быть корректным description");
        validate(description, "У события должно быть корректным description");
        validate(location, "У события должно быть корректным location");
        validate(eventDate, "У события должно быть корректным eventDate");
        this.id = id;
        this.initiator = initiator;
        this.category = category;
        this.createdOn = createdOn;
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.location = location;
        this.views = views;
        this.confirmedRequests = confirmedRequests;
        this.publishedOn = publishedOn;
        this.state = state;
    }
}
