package ru.practicum.explore_with_me.ewm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventFullDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventShortDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventStateDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.NewEventDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.SortOptions;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventAdminDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.UpdateEventUserDto;
import ru.practicum.explore_with_me.ewm.api.mapper.EventApiMapper;
import ru.practicum.explore_with_me.ewm.api.mapper.StateApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.CategoryService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class EventController {
    private final EventService eventService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final EventApiMapper eventMapper;
    private final StateApiMapper stateMapper;

    @Operation(summary = "Получение полной информации о событии добавленном текущим пользователем",
            description = "В случае, если события с заданным id не найдено, возвращает статус код 404",
            tags = "Private: События")
    @GetMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> getById(@Parameter(description = "id текущего пользователя", required = true)
                                                    @PathVariable("userId")
                                                    long userId,
                                                @Parameter(description = "id события", required = true)
                                                @PathVariable("eventId")
                                                long eventId,
                                                HttpServletRequest request) {
        User initiator = userService.findById(userId);
        return ResponseEntity.ok(eventMapper.toFullDto(eventService.findByIdAndUser(eventId, initiator)));
    }

    @Operation(summary = "Получение подробной информации об опубликованном событии по его идентификатору",
            description = "Обратите внимание:\\n- событие должно быть опубликовано\\n- информация о событии должна включать в себя количество просмотров и количество подтвержденных запросов\\n- информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики\\n\\nВ случае, если события с заданным id не найдено, возвращает статус код 404",
            tags = "Public: События")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventFullDto> getById(@Parameter(description = "id события", required = true)
                                                    @PathVariable("eventId")
                                                    long eventId,
                                                HttpServletRequest request) {
        Event event = eventService.findById(eventId);
        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new NotFoundException("Такое событие еще не опубликовано");
        }
        return ResponseEntity.ok(eventMapper.toFullDto(event));
    }

    @Operation(summary = "Получение событий, добавленных текущим пользователем",
            description = "В случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список",
            tags = "Private: События")
    @GetMapping("/users/{userId}/events")
    public ResponseEntity<List<EventShortDto>> findByUser(@Parameter(description = "id текущего пользователя", required = true)
                                                              @PathVariable("userId")
                                                              long userId,
                                                          @Parameter(description = "количество событий, которые нужно пропустить для формирования текущего набора", required = false)
                                                          @RequestParam(value = "from", defaultValue = "0", required = false)
                                                          Integer from,
                                                          @Parameter(description = "количество событий в наборе", required = false)
                                                              @RequestParam(value = "size", defaultValue = "10", required = false)
                                                              Integer size,
                                                          HttpServletRequest request) {
        User initiator = userService.findById(userId);
        List<Event> events = eventService.findByInitiator(initiator, from, size);
        return ResponseEntity.ok(eventMapper.toShortDto(events));
    }

    @Operation(summary = "Поиск событий",
            description = "Эндпоинт возвращает полную информацию обо всех событиях подходящих под переданные условия\\n\\nВ случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список",
            tags = "Admin: События")
    @GetMapping("/admin/events")
    public ResponseEntity<List<EventFullDto>> adminSearch(@Parameter(description = "список id пользователей, чьи события нужно найти", required = false)
                                                              @RequestParam(value = "users", required = false)
                                                              List<Long> users,
                                                          @Parameter(description = "список состояний в которых находятся искомые события", required = false)
                                                            @RequestParam(value = "states", required = false)
                                                            List<EventStateDto> stateDtos,
                                                          @Parameter(description = "список id категорий в которых будет вестись поиск", required = false)
                                                              @RequestParam(value = "categories", required = false)
                                                              List<Long> categories,
                                                          @Parameter(description = "дата и время не раньше которых должно произойти событие", required = false)
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                              @RequestParam(value = "rangeStart", required = false)
                                                              LocalDateTime rangeStart,
                                                          @Parameter(description = "дата и время не позже которых должно произойти событие", required = false)
                                                              @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                              @RequestParam(value = "rangeEnd", required = false)
                                                              LocalDateTime rangeEnd,
                                                          @Parameter(description = "количество событий, которые нужно пропустить для формирования текущего набора", required = false)
                                                              @RequestParam(value = "from", defaultValue = "0", required = false)
                                                              Integer from,
                                                          @Parameter(description = "количество событий в наборе", required = false)
                                                              @RequestParam(value = "size", defaultValue = "10", required = false)
                                                              Integer size,
                                                          HttpServletRequest request) {
        List<EventState> states = null;
        if (Objects.nonNull(stateDtos)) {
            states = stateMapper.toModel(stateDtos);
        }
        List<Event> events = eventService.findByFilter(users, states, categories, rangeStart, rangeEnd, from, size);
        return ResponseEntity.ok(eventMapper.toFullDto(events));
    }

    @Operation(summary = "Получение событий с возможностью фильтрации",
            description = "Обратите внимание: \\n- это публичный эндпоинт, соответственно в выдаче должны быть только опубликованные события\\n- текстовый поиск (по аннотации и подробному описанию) должен быть без учета регистра букв\\n- если в запросе не указан диапазон дат [rangeStart-rangeEnd], то нужно выгружать события, которые произойдут позже текущей даты и времени\\n- информация о каждом событии должна включать в себя количество просмотров и количество уже одобренных заявок на участие\\n- информацию о том, что по этому эндпоинту был осуществлен и обработан запрос, нужно сохранить в сервисе статистики\\n\\nВ случае, если по заданным фильтрам не найдено ни одного события, возвращает пустой список",
            tags = "Public: События")
    @GetMapping("/events")
    public ResponseEntity<List<EventFullDto>> search(@Parameter(description = "текст для поиска в содержимом аннотации и подробном описании события", required = false)
                                                         @RequestParam(value = "text", required = false)
                                                         String text,
                                                     @Parameter(description = "список идентификаторов категорий в которых будет вестись поиск", required = false)
                                                     @RequestParam(value = "categories", required = false)
                                                     List<Long> categories,
                                                     @Parameter(description = "поиск только платных/бесплатных событий", required = false)
                                                         @RequestParam(value = "paid", required = false)
                                                         Boolean paid,
                                                     @Parameter(description = "дата и время не раньше которых должно произойти событие", required = false)
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "rangeStart", required = false)
                                                         LocalDateTime rangeStart,
                                                     @Parameter(description = "дата и время не позже которых должно произойти событие", required = false)
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") @RequestParam(value = "rangeEnd", required = false)
                                                         LocalDateTime rangeEnd,
                                                     @Parameter(description = "только события у которых не исчерпан лимит запросов на участие", required = false)
                                                         @RequestParam(value = "onlyAvailable", required = false)
                                                         Boolean onlyAvailable,
                                                     @Parameter(description = "Вариант сортировки: по дате события или по количеству просмотров", required = false)
                                                         @RequestParam(value = "sort", required = false)
                                                         SortOptions sort,
                                                     @Parameter(description = "количество событий, которые нужно пропустить для формирования текущего набора", required = false)
                                                         @RequestParam(value = "from", defaultValue = "0", required = false)
                                                         Integer from,
                                                     @Parameter(description = "количество событий в наборе", required = false)
                                                         @RequestParam(value = "size", defaultValue = "10", required = false)
                                                         Integer size,
                                                     HttpServletRequest request) {
        if (Objects.nonNull(rangeEnd) && rangeEnd.isBefore(LocalDateTime.now())) {
            throw new ValidationException("Дата конца не должна быть в прошлом");
        }
        List<Event> events = eventService.findByFilter(text,
                paid,
                categories,
                rangeStart, rangeEnd,
                onlyAvailable,
                from, size,
                sort == null || !sort.equals(SortOptions.VIEWS));
        return ResponseEntity.ok(eventMapper.toFullDto(events));
    }

    @Operation(summary = "Добавление нового события",
            description = "Обратите внимание: дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента",
            tags = "Private: События")
    @PostMapping("/users/{userId}/events")
    public ResponseEntity<EventFullDto> create(@Parameter(description = "id текущего пользователя", required = true)
                                                   @PathVariable
                                                   long userId,
                                               @RequestBody @Valid NewEventDto dto,
                                               HttpServletRequest request) {
        User initiator = userService.findById(userId);
        Category category = categoryService.findById(dto.getCategory());
        NewEvent event = eventMapper.toNewEvent(dto, initiator, category);
        Event savedEvent = eventService.create(event);
        return ResponseEntity
                .created(URI.create("/users/" + userId + "/events/" + savedEvent.getId()))
                .body(eventMapper.toFullDto(savedEvent));
    }

    @Operation(summary = "Изменение события добавленного текущим пользователем",
            description = "Обратите внимание:\\n- изменить можно только отмененные события или события в состоянии ожидания модерации (Ожидается код ошибки 409)\\n- дата и время на которые намечено событие не может быть раньше, чем через два часа от текущего момента (Ожидается код ошибки 409)\\n",
            tags = "Private: События")
    @PatchMapping("/users/{userId}/events/{eventId}")
    public ResponseEntity<EventFullDto> update(@Parameter(description = "id текущего пользователя", required = true)
                                                   @PathVariable("userId")
                                                   long userId,
                                               @Parameter(description = "id события", required = true)
                                               @PathVariable("eventId")
                                               long eventId,
                                               @RequestBody @Valid UpdateEventUserDto dto,
                                               HttpServletRequest request) {
        User initiator = userService.findById(userId);
        Event event = eventService.findByIdAndUser(eventId, initiator);
        Category category = null;
        if (Objects.nonNull(dto.getCategory())) {
            category = categoryService.findById(dto.getCategory());
        }
        if (event.getState() != EventState.CANCEL_REVIEW
                && event.getState() != EventState.WAITING_FOR_PUBLICATION
                && event.getState() != EventState.PUBLICATION_CANCELED) {
            throw new ForbiddenException("Only pending or canceled events can be changed");
        }
        Event eventToBeUpdated = eventMapper.updateEventFromUserRequest(event, dto, category);
        Event savedEvent = eventService.update(eventToBeUpdated);
        return ResponseEntity.ok(eventMapper.toFullDto(savedEvent));
    }

    @Operation(summary = "Редактирование данных события и его статуса (отклонение/публикация).",
            description = "Редактирование данных любого события администратором. Валидация данных не требуется.\\nОбратите внимание:\\n - дата начала изменяемого события должна быть не ранее чем за час от даты публикации. (Ожидается код ошибки 409)\\n- событие можно публиковать, только если оно в состоянии ожидания публикации (Ожидается код ошибки 409)\\n- событие можно отклонить, только если оно еще не опубликовано (Ожидается код ошибки 409)",
            tags = "Admin: События")
    @PatchMapping("/admin/events/{eventId}")
    public ResponseEntity<EventFullDto> review(@Parameter(description = "id события", required = true)
                                                   @PathVariable("eventId") long eventId,
                                               @RequestBody @Valid UpdateEventAdminDto dto,
                                               HttpServletRequest request) {
        Event event = eventService.findById(eventId);
        Category category = null;
        if (Objects.nonNull(dto.getCategory())) {
            category = categoryService.findById(dto.getCategory());
        }
        Event eventToBeUpdated = eventMapper.updateEventFromAdminRequest(event, dto, category);
        Event savedEvent = eventService.update(eventToBeUpdated);
        return ResponseEntity.ok(eventMapper.toFullDto(savedEvent));
    }
}
