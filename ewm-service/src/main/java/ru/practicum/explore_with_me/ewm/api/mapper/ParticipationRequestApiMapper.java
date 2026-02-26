package ru.practicum.explore_with_me.ewm.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateRequest;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.NewParticipationRequestDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.ParticipationRequestDto;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ParticipationRequestApiMapper {
    public NewParticipationRequest toNewRequestModel(NewParticipationRequestDto dto, Event event, User participant) {
        if (dto == null) {
            return null;
        }

        return new NewParticipationRequest(
                event,
                participant,
                RequestStatus.PENDING,  // Новые заявки всегда создаются со статусом PENDING
                LocalDateTime.now()
        );
    }

    public ParticipationRequestDto toDto(ParticipationRequest model) {
        if (model == null) {
            return null;
        }

        return ParticipationRequestDto.builder()
                .id(model.getId())
                .event(model.getEvent().getId())
                .requester(model.getRequester().getId())
                .status(model.getStatus())
                .created(model.getCreated())
                .build();
    }

    // Преобразование списка моделей в список DTO
    public List<ParticipationRequestDto> toDtoList(List<ParticipationRequest> models) {
        if (models == null) {
            return null;
        }

        return models.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public EventRequestStatusUpdateRequest toStatusUpdateModel(EventRequestStatusUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        // Создаем объект модели (нужно будет добавить соответствующий класс модели)
        return EventRequestStatusUpdateRequest.builder()
                .requestIds(dto.getRequestIds())
                .status(mapToRequestStatus(dto.getStatus()))
                .build();
    }

    // Преобразование результата обновления статусов в DTO
    public EventRequestStatusUpdateResultDto toStatusUpdateResultDto(
            List<ParticipationRequest> confirmed,
            List<ParticipationRequest> rejected) {

        return EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(toDtoList(confirmed))
                .rejectedRequests(toDtoList(rejected))
                .build();
    }

    public RequestStatus mapToRequestStatus(EventRequestStatusUpdateRequestDto.RequestStatusUpdate status) {
        if (status == null) {
            return null;
        }

        switch (status) {
            case CONFIRMED:
                return RequestStatus.CONFIRMED;
            case REJECTED:
                return RequestStatus.REJECTED;
            default:
                throw new MappingException("Unknown status update value: " + status);
        }
    }

    public static class MappingException extends RuntimeException {
        public MappingException(String message) {
            super(message);
        }
    }
}
