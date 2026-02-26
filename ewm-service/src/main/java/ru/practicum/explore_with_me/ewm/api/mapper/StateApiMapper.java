package ru.practicum.explore_with_me.ewm.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventStateDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventAdminDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventUserDto;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.explore_with_me.ewm.domain.model.event.EventState.*;

@Component
public class StateApiMapper {
    public EventStateDto mapEventState(EventState state) {
        if (state == null) {
            return null;
        }

        switch (state) {
            case WAITING_FOR_PUBLICATION:
                return EventStateDto.PENDING;
            case CANCEL_REVIEW:
                return EventStateDto.CANCELED;
            case PUBLISHED:
                return EventStateDto.PUBLISHED;
            case PUBLICATION_CANCELED:
                return EventStateDto.CANCELED;
            default:
                throw new IllegalArgumentException("Unknown event state: " + state);
        }
    }

    public EventState toModel(EventStateDto state) {
        if (state == null) {
            return null;
        }

        switch (state) {
            case PENDING:
                return WAITING_FOR_PUBLICATION;
            case PUBLISHED:
                return PUBLISHED;
            case CANCELED:
                return PUBLICATION_CANCELED;
            default:
                throw new IllegalArgumentException("Unknown event state: " + state);
        }
    }

    public List<EventState> toModel(List<EventStateDto> dtos) {
        return dtos.stream().map(this::toModel).collect(Collectors.toList());
    }

    public EventState updateStateFromUserAction(EventState currentState, UpdateEventUserDto.UserStateAction action) {
        if (action == null) {
            return currentState;
        }

        switch (action) {
            case SEND_TO_REVIEW:
                return WAITING_FOR_PUBLICATION;
            case CANCEL_REVIEW:
                return CANCEL_REVIEW;
            default:
                return currentState;
        }
    }

    public EventState updateStateFromAdminAction(EventState currentState, UpdateEventAdminDto.AdminStateAction action) {
        if (action == null) {
            return currentState;
        }

        switch (action) {
            case PUBLISH_EVENT:
                if (currentState == WAITING_FOR_PUBLICATION) {
                    return PUBLISHED;
                }
                return currentState;
            case REJECT_EVENT:
                if (currentState == WAITING_FOR_PUBLICATION) {
                    return EventState.PUBLICATION_CANCELED;
                }
                return currentState;
            default:
                return currentState;
        }
    }
}
