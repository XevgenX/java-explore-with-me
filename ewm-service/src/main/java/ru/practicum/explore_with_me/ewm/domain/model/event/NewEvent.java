package ru.practicum.explore_with_me.ewm.domain.model.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.DateInFutureValidatable;
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
public class NewEvent implements DateInFutureValidatable, ObjectValidatable, TextValidatable {
    private final User initiator;
    private final Category category;
    private final String title;
    private final String annotation;
    private final String description;
    private final LocalDateTime eventDate;
    private final Boolean paid;
    private final Integer participantLimit;
    private final Boolean requestModeration;
    private final NewLocation location;

    public NewEvent(User initiator, Category category,
                    String title, String annotation, String description,
                    LocalDateTime eventDate, Boolean paid, Integer participantLimit, Boolean requestModeration,
                    NewLocation location) {
        validate(initiator, "У события должно быть корректным initiator");
        validate(category, "У события должно быть корректным category");
        validate(annotation, "У события должно быть корректным annotation");
        validate(description, "У события должно быть корректным description");
        validate(description, "У события должно быть корректным description");
        validate(location, "У события должно быть корректным location");
        validate(eventDate, "У события должно быть корректным location");
        this.initiator = initiator;
        this.category = category;
        this.title = title;
        this.annotation = annotation;
        this.description = description;
        this.eventDate = eventDate;
        this.paid = paid;
        this.participantLimit = participantLimit;
        this.requestModeration = requestModeration;
        this.location = location;
    }
}
