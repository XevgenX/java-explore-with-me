package ru.practicum.explore_with_me.ewm.domain.model.comment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.NowDateValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class NewComment implements ObjectValidatable, TextValidatable, NowDateValidatable {
    private final String text;
    private final Event event;
    private final User author;
    private final LocalDateTime createdOn;

    public NewComment(String text, Event event, User author, LocalDateTime createdOn) {
        validate(event, "У комментария должен быть корректным event");
        validate(author, "У комментария должен быть корректным user");
        validate(text, "У комментария должен быть корректным text");
        validate(createdOn, "У комментария должен быть корректным createdOn");
        this.text = text;
        this.event = event;
        this.author = author;
        this.createdOn = createdOn;
    }
}
