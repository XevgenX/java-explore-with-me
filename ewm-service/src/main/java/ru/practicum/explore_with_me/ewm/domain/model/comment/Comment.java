package ru.practicum.explore_with_me.ewm.domain.model.comment;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
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
public class Comment implements IdValidatable, ObjectValidatable, TextValidatable, NowDateValidatable {
    private final Long id;
    private String text;
    private Event event;
    private User author;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CommentStatus status;

    public Comment(Long id, String text, Event event, User author, LocalDateTime createdOn, LocalDateTime updatedOn, CommentStatus status) {
        validate(id, "У комментария должен быть корректным id");
        validate(event, "У комментария должен быть корректным event");
        validate(author, "У комментария должен быть корректным user");
        validate(text, "У комментария должен быть корректным text");
        validate(createdOn, "У комментария должен быть корректным createdOn");
        validate(status, "У комментария должен быть корректным status");
        this.id = id;
        this.text = text;
        this.event = event;
        this.author = author;
        this.createdOn = createdOn;
        this.updatedOn = updatedOn;
        this.status = status;
    }
}
