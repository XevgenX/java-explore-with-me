package ru.practicum.explore_with_me.ewm.domain.model.request;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class NewParticipationRequest implements IdValidatable, ObjectValidatable {
    private Event event;
    private User requester;
    private RequestStatus status;
    private LocalDateTime created;

    public NewParticipationRequest(Event event, User requester, RequestStatus status, LocalDateTime created) {
        validate(event, "У запроса должно быть корректным event");
        validate(requester, "У запроса должно быть корректным requester");
        validate(status, "У запроса должно быть корректным status");
        this.event = event;
        this.requester = requester;
        this.status = status;
        this.created = created;
    }
}
