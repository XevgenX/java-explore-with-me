package ru.practicum.explore_with_me.ewm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateResultDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.ParticipationRequestDto;
import ru.practicum.explore_with_me.ewm.api.mapper.ParticipationRequestApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.ParticipationService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ParticipationController {
    private final ParticipationService participationService;
    private final EventService eventService;
    private final UserService userService;
    private final ParticipationRequestApiMapper mapper;

    @Operation(summary = "Получение информации о заявках текущего пользователя на участие в чужих событиях",
            description = "В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список",
            tags = "Private: Запросы на участие")
    @GetMapping("/users/{userId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getByCurrentUser(@Parameter(description = "id текущего пользователя", required = true)
                                                                              @PathVariable
                                                                              Long userId,
                                                                          HttpServletRequest request) {
        List<ParticipationRequest> requests = participationService.findByUser(userService.findById(userId));
        return ResponseEntity.ok(mapper.toDtoList(requests));
    }

    @Operation(summary = "Получение информации о запросах на участие в событии текущего пользователя",
            description = "В случае, если по заданным фильтрам не найдено ни одной заявки, возвращает пустой список",
            tags = "Private: События")
    @GetMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getByCurrentUser(@Parameter(description = "id текущего пользователя", required = true)
                                                                              @PathVariable
                                                                              Long userId,
                                                                          @Parameter(description = "id события", required = true)
                                                                          @PathVariable
                                                                          Long eventId,
                                                                          HttpServletRequest request) {
        User initiator = userService.findById(userId);
        Event event = eventService.findByIdAndUser(eventId, initiator);
        List<ParticipationRequest> requests = participationService.findByEvent(event);
        return ResponseEntity.ok(mapper.toDtoList(requests));
    }

    @Operation(summary = "Добавление запроса от текущего пользователя на участие в событии",
            description = "Обратите внимание:\\n- нельзя добавить повторный запрос  (Ожидается код ошибки 409)\\n- инициатор события не может добавить запрос на участие в своём событии (Ожидается код ошибки 409)\\n- нельзя участвовать в неопубликованном событии (Ожидается код ошибки 409)\\n- если у события достигнут лимит запросов на участие - необходимо вернуть ошибку  (Ожидается код ошибки 409)\\n- если для события отключена пре-модерация запросов на участие, то запрос должен автоматически перейти в состояние подтвержденного",
            tags = "Private: Запросы на участие")
    @PostMapping("/users/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(@Parameter(description = "id текущего пользователя", required = true)
                                                                               @PathVariable
                                                                               Long userId,
                                                                           @Parameter(description = "id события", required = true)
                                                                               @RequestParam
                                                                               Long eventId,
                                                                           HttpServletRequest httRequest) {
        LocalDateTime requestTime = LocalDateTime.now().withNano(
                LocalDateTime.now().getNano() / 1000 * 1000
        );
        User participant = userService.findById(userId);
        Event event = eventService.findById(eventId);
        NewParticipationRequest request = NewParticipationRequest.builder()
                .event(event)
                .requester(participant)
                .status(RequestStatus.PENDING)
                .created(requestTime)
                .build();
        ParticipationRequest createdRequest = participationService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDto(createdRequest));
    }

    @Operation(summary = "Отмена своего запроса на участие в событии",
            tags = "Private: Запросы на участие")
    @PatchMapping("/users/{userId}/requests/{requestId}/cancel")
    public ResponseEntity<ParticipationRequestDto> cancelRequest(@Parameter(description = "id текущего пользователя", required = true)
                                                                     @PathVariable
                                                                     Long userId,
                                                                 @Parameter(description = "id запроса на участие", required = true)
                                                                 @PathVariable
                                                                 Long requestId,
                                                                 HttpServletRequest request) {
        User participant = userService.findById(userId);
        ParticipationRequest cancelledRequest = participationService.changeStatus(participant, requestId, RequestStatus.CANCELED);
        return ResponseEntity.ok(mapper.toDto(cancelledRequest));
    }

    @Operation(summary = "Изменение статуса (подтверждена, отменена) заявок на участие в событии текущего пользователя",
            description = "Обратите внимание:\\n- если для события лимит заявок равен 0 или отключена пре-модерация заявок, то подтверждение заявок не требуется\\n- нельзя подтвердить заявку, если уже достигнут лимит по заявкам на данное событие (Ожидается код ошибки 409)\\n- статус можно изменить только у заявок, находящихся в состоянии ожидания (Ожидается код ошибки 409)\\n- если при подтверждении данной заявки, лимит заявок для события исчерпан, то все неподтверждённые заявки необходимо отклонить",
            tags = "Private: События")
    @PatchMapping("/users/{userId}/events/{eventId}/requests")
    public ResponseEntity<EventRequestStatusUpdateResultDto> updateRequests(@Parameter(description = "id текущего пользователя", required = true)
                                                                                @PathVariable
                                                                                Long userId,
                                                                            @Parameter(description = "id события текущего пользователя", required = true)
                                                                            @PathVariable
                                                                            Long eventId,
                                                                            @RequestBody(required = false)
                                                                                @Valid EventRequestStatusUpdateRequestDto dto,
            HttpServletRequest httpRequest) {
        if (Objects.isNull(dto)) {
            throw new ForbiddenException("");
        }
        Event event = eventService.findById(eventId);
        User initiator = userService.findById(userId);
        participationService.validateThatUserInitiateEvent(initiator, event);
        dto.getRequestIds().forEach(requestId -> {
            ParticipationRequest request = participationService.findById(requestId);
            participationService.validateThatRequestForNeededEvent(request, event);
            participationService.changeStatus(requestId, mapper.mapToRequestStatus(dto.getStatus()));
        });
        List<ParticipationRequest> requests = participationService.findByEvent(event);
        EventRequestStatusUpdateResultDto resultDto = EventRequestStatusUpdateResultDto.builder()
                .confirmedRequests(mapper.toDtoList(requests.stream()
                        .filter(r -> r.getStatus().equals(RequestStatus.CONFIRMED))
                        .toList()))
                .rejectedRequests(mapper.toDtoList(requests.stream()
                        .filter(r -> r.getStatus().equals(RequestStatus.REJECTED))
                        .toList()))
                .build();
        return ResponseEntity.ok(resultDto);
    }
}
