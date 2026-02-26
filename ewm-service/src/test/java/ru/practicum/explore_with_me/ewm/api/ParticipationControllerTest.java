package ru.practicum.explore_with_me.ewm.api;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.api.dto.request.EventRequestStatusUpdateRequestDto;
import ru.practicum.explore_with_me.ewm.api.dto.request.ParticipationRequestDto;
import ru.practicum.explore_with_me.ewm.api.mapper.ParticipationRequestApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.ParticipationService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера запросов на участие")
public class ParticipationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ParticipationService participationService;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserService userService;

    @MockBean
    private ParticipationRequestApiMapper mapper;

    private User testUser;
    private User testInitiator;
    private Event testEvent;
    private ParticipationRequest testRequest;
    private ParticipationRequestDto testRequestDto;
    private EventRequestStatusUpdateRequestDto testUpdateRequestDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(2L)
                .name("Участник")
                .email("participant@example.com")
                .build();

        testInitiator = User.builder()
                .id(1L)
                .name("Инициатор")
                .email("initiator@example.com")
                .build();

        var testCategory = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        var testLocation = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Концерт")
                .annotation("Аннотация концерта")
                .description("Описание концерта")
                .initiator(testInitiator)
                .category(testCategory)
                .location(testLocation)
                .eventDate(LocalDateTime.now().plusDays(10))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .views(0L)
                .confirmedRequests(0)
                .build();

        testRequest = ParticipationRequest.builder()
                .id(1L)
                .event(testEvent)
                .requester(testUser)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        testRequestDto = ParticipationRequestDto.builder()
                .id(1L)
                .event(1L)
                .requester(2L)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();

        testUpdateRequestDto = EventRequestStatusUpdateRequestDto.builder()
                .requestIds(List.of(1L, 2L))
                .status(EventRequestStatusUpdateRequestDto.RequestStatusUpdate.CONFIRMED)
                .build();
    }

    // ==================== GET /users/{userId}/requests ====================
    @Test
    @DisplayName("GET /users/{userId}/requests - должен вернуть запросы пользователя")
    void getByCurrentUser_ShouldReturnUserRequests() throws Exception {
        List<ParticipationRequest> requests = List.of(testRequest);
        List<ParticipationRequestDto> requestDtos = List.of(testRequestDto);

        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(participationService.findByUser(testUser)).thenReturn(requests);
        Mockito.when(mapper.toDtoList(requests)).thenReturn(requestDtos);

        mockMvc.perform(get("/users/2/requests")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].event", is(1)))
                .andExpect(jsonPath("$[0].requester", is(2)));

        Mockito.verify(userService, Mockito.times(1)).findById(2L);
        Mockito.verify(participationService, Mockito.times(1)).findByUser(testUser);
    }

    @Test
    @DisplayName("GET /users/{userId}/requests - должен вернуть пустой список если нет запросов")
    void getByCurrentUser_ShouldReturnEmptyListWhenNoRequests() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(participationService.findByUser(testUser)).thenReturn(List.of());
        Mockito.when(mapper.toDtoList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/users/2/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /users/{userId}/requests - должен вернуть 404 при несуществующем пользователе")
    void getByCurrentUser_ShouldReturn404WhenUserNotFound() throws Exception {
        Mockito.when(userService.findById(999L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999/requests"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /users/{userId}/events/{eventId}/requests ====================
    @Test
    @DisplayName("GET /users/{userId}/events/{eventId}/requests - должен вернуть запросы на событие")
    void getByCurrentUser_WithEvent_ShouldReturnEventRequests() throws Exception {
        List<ParticipationRequest> requests = List.of(testRequest);
        List<ParticipationRequestDto> requestDtos = List.of(testRequestDto);

        Mockito.when(userService.findById(1L)).thenReturn(testInitiator);
        Mockito.when(eventService.findByIdAndUser(1L, testInitiator)).thenReturn(testEvent);
        Mockito.when(participationService.findByEvent(testEvent)).thenReturn(requests);
        Mockito.when(mapper.toDtoList(requests)).thenReturn(requestDtos);

        mockMvc.perform(get("/users/1/events/1/requests"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        Mockito.verify(userService, Mockito.times(1)).findById(1L);
        Mockito.verify(eventService, Mockito.times(1)).findByIdAndUser(1L, testInitiator);
        Mockito.verify(participationService, Mockito.times(1)).findByEvent(testEvent);
    }

    @Test
    @DisplayName("GET /users/{userId}/events/{eventId}/requests - должен вернуть 403 если пользователь не инициатор")
    void getByCurrentUser_WithEvent_ShouldReturn403WhenUserNotInitiator() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(eventService.findByIdAndUser(1L, testUser))
                .thenThrow(new ForbiddenException("User is not initiator"));

        mockMvc.perform(get("/users/2/events/1/requests"))
                .andExpect(status().isConflict()); // или isForbidden() в зависимости от маппинга
    }

    // ==================== POST /users/{userId}/requests ====================
    @Test
    @DisplayName("POST /users/{userId}/requests - должен создать запрос на участие")
    void addParticipationRequest_ShouldCreateRequest() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(participationService.create(Mockito.any(NewParticipationRequest.class)))
                .thenReturn(testRequest);
        Mockito.when(mapper.toDto(testRequest)).thenReturn(testRequestDto);

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.event", is(1)))
                .andExpect(jsonPath("$.requester", is(2)));

        Mockito.verify(userService, Mockito.times(1)).findById(2L);
        Mockito.verify(eventService, Mockito.times(1)).findById(1L);
        Mockito.verify(participationService, Mockito.times(1)).create(Mockito.any(NewParticipationRequest.class));
    }

    @Test
    @DisplayName("POST /users/{userId}/requests - должен вернуть 404 при несуществующем событии")
    void addParticipationRequest_ShouldReturn404WhenEventNotFound() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(eventService.findById(999L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /users/{userId}/requests - должен вернуть 409 при повторном запросе")
    void addParticipationRequest_ShouldReturn409WhenDuplicateRequest() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(participationService.create(Mockito.any(NewParticipationRequest.class)))
                .thenThrow(new ForbiddenException("Request already exists"));

        mockMvc.perform(post("/users/2/requests")
                        .param("eventId", "1"))
                .andExpect(status().isConflict());
    }

    // ==================== PATCH /users/{userId}/requests/{requestId}/cancel ====================
    @Test
    @DisplayName("PATCH /users/{userId}/requests/{requestId}/cancel - должен отменить запрос")
    void cancelRequest_ShouldCancelRequest() throws Exception {
        ParticipationRequest cancelledRequest = testRequest;
        cancelledRequest.setStatus(RequestStatus.CANCELED);
        ParticipationRequestDto cancelledRequestDto = testRequestDto;
        cancelledRequestDto.setStatus(RequestStatus.CANCELED);

        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(participationService.changeStatus(testUser, 1L, RequestStatus.CANCELED))
                .thenReturn(cancelledRequest);
        Mockito.when(mapper.toDto(cancelledRequest)).thenReturn(cancelledRequestDto);

        mockMvc.perform(patch("/users/2/requests/1/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("CANCELED")));

        Mockito.verify(participationService, Mockito.times(1))
                .changeStatus(testUser, 1L, RequestStatus.CANCELED);
    }

    @Test
    @DisplayName("PATCH /users/{userId}/requests/{requestId}/cancel - должен вернуть 404 при несуществующем запросе")
    void cancelRequest_ShouldReturn404WhenRequestNotFound() throws Exception {
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.when(participationService.changeStatus(testUser, 999L, RequestStatus.CANCELED))
                .thenThrow(new NotFoundException("Request not found"));

        mockMvc.perform(patch("/users/2/requests/999/cancel"))
                .andExpect(status().isNotFound());
    }

    // ==================== PATCH /users/{userId}/events/{eventId}/requests ====================
    @Test
    @DisplayName("PATCH /users/{userId}/events/{eventId}/requests - должен обновить статусы запросов")
    void updateRequests_ShouldUpdateRequestStatuses() throws Exception {
        ParticipationRequest confirmedRequest = testRequest;
        confirmedRequest.setStatus(RequestStatus.CONFIRMED);
        ParticipationRequest rejectedRequest = testRequest;
        rejectedRequest.setId(2L);
        rejectedRequest.setStatus(RequestStatus.REJECTED);

        List<ParticipationRequest> allRequests = List.of(confirmedRequest, rejectedRequest);
        List<ParticipationRequestDto> confirmedDtos = List.of(testRequestDto);
        List<ParticipationRequestDto> rejectedDtos = List.of(ParticipationRequestDto.builder()
                .id(2L).event(1L).requester(3L).status(RequestStatus.REJECTED).build());

        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(userService.findById(1L)).thenReturn(testInitiator);
        Mockito.doNothing().when(participationService).validateThatUserInitiateEvent(testInitiator, testEvent);

        Mockito.when(participationService.findById(1L)).thenReturn(testRequest);
        Mockito.when(participationService.findById(2L)).thenReturn(rejectedRequest);
        Mockito.doNothing().when(participationService).validateThatRequestForNeededEvent(any(), eq(testEvent));
        Mockito.when(participationService.changeStatus(1L, RequestStatus.CONFIRMED)).thenReturn(confirmedRequest);
        Mockito.when(participationService.changeStatus(2L, RequestStatus.CONFIRMED)).thenReturn(rejectedRequest);

        Mockito.when(participationService.findByEvent(testEvent)).thenReturn(allRequests);
        Mockito.when(mapper.mapToRequestStatus(EventRequestStatusUpdateRequestDto.RequestStatusUpdate.CONFIRMED)).thenReturn(RequestStatus.CONFIRMED);
        Mockito.when(mapper.toDtoList(Mockito.anyList())).thenReturn(confirmedDtos, rejectedDtos);

        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.confirmedRequests", hasSize(1)))
                .andExpect(jsonPath("$.rejectedRequests", hasSize(1)));

        Mockito.verify(participationService, Mockito.times(2)).changeStatus(anyLong(), Mockito.any(RequestStatus.class));
    }

    @Test
    @DisplayName("PATCH /users/{userId}/events/{eventId}/requests - должен вернуть 400 при пустом теле")
    void updateRequests_ShouldReturn400WhenBodyEmpty() throws Exception {
        mockMvc.perform(patch("/users/1/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isConflict()); // ForbiddenException с пустым сообщением
    }

    @Test
    @DisplayName("PATCH /users/{userId}/events/{eventId}/requests - должен вернуть 403 если пользователь не инициатор")
    void updateRequests_ShouldReturn403WhenUserNotInitiator() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(userService.findById(2L)).thenReturn(testUser);
        Mockito.doThrow(new ForbiddenException("User is not initiator"))
                .when(participationService).validateThatUserInitiateEvent(testUser, testEvent);

        mockMvc.perform(patch("/users/2/events/1/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateRequestDto)))
                .andExpect(status().isConflict());
    }

    // ==================== Тесты обработки ошибок ====================
    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке")
    void shouldReturn500OnUnexpectedError() throws Exception {
        Mockito.when(userService.findById(2L)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/users/2/requests"))
                .andExpect(status().isInternalServerError());
    }
}
