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
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.api.dto.category.CategoryDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.*;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;
import ru.practicum.explore_with_me.ewm.api.mapper.EventApiMapper;
import ru.practicum.explore_with_me.ewm.api.mapper.StateApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.CategoryService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера событий")
public class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserService userService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private EventApiMapper eventMapper;

    @MockBean
    private StateApiMapper stateMapper;

    private User testUser;
    private Category testCategory;
    private Event testEvent;
    private EventFullDto testEventFullDto;
    private EventShortDto testEventShortDto;
    private NewEventDto testNewEventDto;
    private UpdateEventUserDto testUpdateEventUserDto;
    private UpdateEventAdminDto testUpdateEventAdminDto;
    private Location testLocation;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan@example.com")
                .build();

        testCategory = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        testLocation = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Концерт")
                .annotation("Аннотация концерта")
                .description("Описание концерта")
                .initiator(testUser)
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

        testEventFullDto = EventFullDto.builder()
                .id(1L)
                .title("Концерт")
                .annotation("Аннотация концерта")
                .description("Описание концерта")
                .initiator(UserDto.builder().id(1L).name("Иван Петров").email("ivan@example.com").build())
                .category(CategoryDto.builder().id(1L).name("Концерты").build())
                .location(LocationDto.builder().lat(55.754167F).lon(37.62F).build())
                .eventDate(LocalDateTime.now().plusDays(10))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventStateDto.PUBLISHED)
                .views(0L)
                .confirmedRequests(0L)
                .build();

        testEventShortDto = EventShortDto.builder()
                .id(1L)
                .title("Концерт")
                .annotation("Аннотация концерта")
                .initiator(UserDto.builder().id(1L).name("Иван Петров").email("ivan@example.com").build())
                .category(CategoryDto.builder().id(1L).name("Концерты").build())
                .eventDate(LocalDateTime.now().plusDays(10))
                .paid(true)
                .views(0L)
                .confirmedRequests(0L)
                .build();

        testNewEventDto = NewEventDto.builder()
                .title("Новый концерт")
                .annotation("Новая аннотация")
                .description("Новое описание")
                .category(1L)
                .location(LocationDto.builder().lat(55.754167F).lon(37.62F).build())
                .eventDate(LocalDateTime.now().plusDays(20))
                .paid(true)
                .participantLimit(50)
                .requestModeration(true)
                .build();

        testUpdateEventUserDto = UpdateEventUserDto.builder()
                .title("Обновленный концерт")
                .annotation("Обновленная аннотация")
                .build();

        testUpdateEventAdminDto = UpdateEventAdminDto.builder()
                .title("Обновленный концерт админом")
                .annotation("Обновленная аннотация админом")
                .stateAction(UpdateEventAdminDto.AdminStateAction.PUBLISH_EVENT)
                .build();
    }

    // ==================== GET /users/{userId}/events/{eventId} ====================
    @Test
    @DisplayName("GET /users/{userId}/events/{eventId} - должен вернуть событие по id для пользователя")
    void getById_WithUser_ShouldReturnEvent() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findByIdAndUser(1L, testUser)).thenReturn(testEvent);
        Mockito.when(eventMapper.toFullDto(testEvent)).thenReturn(testEventFullDto);

        mockMvc.perform(get("/users/1/events/1")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Концерт")));

        Mockito.verify(userService, Mockito.times(1)).findById(1L);
        Mockito.verify(eventService, Mockito.times(1)).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("GET /users/{userId}/events/{eventId} - должен вернуть 404 при несуществующем пользователе")
    void getById_WithUser_ShouldReturn404WhenUserNotFound() throws Exception {
        Mockito.when(userService.findById(999L)).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999/events/1"))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).findById(999L);
        Mockito.verify(eventService, Mockito.never()).findByIdAndUser(Mockito.anyLong(), Mockito.any());
    }

    @Test
    @DisplayName("GET /users/{userId}/events/{eventId} - должен вернуть 404 при несуществующем событии")
    void getById_WithUser_ShouldReturn404WhenEventNotFound() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findByIdAndUser(999L, testUser))
                .thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/users/1/events/999"))
                .andExpect(status().isNotFound());

        Mockito.verify(userService, Mockito.times(1)).findById(1L);
        Mockito.verify(eventService, Mockito.times(1)).findByIdAndUser(999L, testUser);
    }

    // ==================== GET /events/{eventId} ====================
    @Test
    @DisplayName("GET /events/{eventId} - должен вернуть опубликованное событие")
    void getById_Public_ShouldReturnPublishedEvent() throws Exception {
        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(eventMapper.toFullDto(testEvent)).thenReturn(testEventFullDto);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.state", is("PUBLISHED")));

        Mockito.verify(eventService, Mockito.times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /events/{eventId} - должен вернуть 404 если событие не опубликовано")
    void getById_Public_ShouldReturn404WhenEventNotPublished() throws Exception {
        Event pendingEvent = testEvent;
        pendingEvent.setState(EventState.WAITING_FOR_PUBLICATION);

        Mockito.when(eventService.findById(1L)).thenReturn(pendingEvent);

        mockMvc.perform(get("/events/1"))
                .andExpect(status().isNotFound());

        Mockito.verify(eventService, Mockito.times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /events/{eventId} - должен вернуть 404 при несуществующем событии")
    void getById_Public_ShouldReturn404WhenEventNotFound() throws Exception {
        Mockito.when(eventService.findById(999L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/events/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /users/{userId}/events ====================
    @Test
    @DisplayName("GET /users/{userId}/events - должен вернуть события пользователя")
    void findByUser_ShouldReturnUserEvents() throws Exception {
        List<Event> events = List.of(testEvent);
        List<EventShortDto> eventDtos = List.of(testEventShortDto);

        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findByInitiator(testUser, 0, 10)).thenReturn(events);
        Mockito.when(eventMapper.toShortDto(events)).thenReturn(eventDtos);

        mockMvc.perform(get("/users/1/events")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        Mockito.verify(eventService, Mockito.times(1)).findByInitiator(testUser, 0, 10);
    }

    @Test
    @DisplayName("GET /users/{userId}/events - должен использовать значения по умолчанию")
    void findByUser_ShouldUseDefaultPagination() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findByInitiator(testUser, 0, 10)).thenReturn(List.of());
        Mockito.when(eventMapper.toShortDto(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isOk());

        Mockito.verify(eventService, Mockito.times(1)).findByInitiator(testUser, 0, 10);
    }

    @Test
    @DisplayName("GET /admin/events - должен работать без параметров")
    void adminSearch_ShouldWorkWithoutParams() throws Exception {
        Mockito.when(eventService.findByFilter(Mockito.isNull(), Mockito.isNull(), Mockito.isNull(),
                        Mockito.isNull(), Mockito.isNull(), Mockito.eq(0), Mockito.eq(10)))
                .thenReturn(List.of());
        Mockito.when(eventMapper.toFullDto(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /events - должен вернуть ошибку при rangeEnd в прошлом")
    void search_Public_ShouldReturnErrorWhenRangeEndInPast() throws Exception {
        LocalDateTime rangeEnd = LocalDateTime.now().minusDays(1);

        mockMvc.perform(get("/events")
                        .param("rangeEnd", rangeEnd.format(DATE_FORMATTER)))
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never()).findByFilter(
                Mockito.anyString(), Mockito.anyBoolean(), Mockito.anyList(),
                Mockito.any(), Mockito.any(), Mockito.anyBoolean(),
                Mockito.anyInt(), Mockito.anyInt(), Mockito.anyBoolean());
    }

    @Test
    @DisplayName("POST /users/{userId}/events - должен вернуть ошибку при невалидных данных")
    void create_ShouldReturnErrorWhenInvalidData() throws Exception {
        NewEventDto invalidDto = NewEventDto.builder()
                .title("")  // пустой заголовок
                .annotation("Аннотация")
                .description("Описание")
                .category(1L)
                .location(LocationDto.builder().lat(55.754167F).lon(37.62F).build())
                .eventDate(LocalDateTime.now().plusDays(20))
                .paid(true)
                .participantLimit(50)
                .requestModeration(true)
                .build();

        mockMvc.perform(post("/users/1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(userService, Mockito.never()).findById(Mockito.anyLong());
    }

    @Test
    @DisplayName("PATCH /users/{userId}/events/{eventId} - должен вернуть ошибку при обновлении опубликованного события")
    void update_ByUser_ShouldReturnErrorWhenEventPublished() throws Exception {
        Mockito.when(userService.findById(1L)).thenReturn(testUser);
        Mockito.when(eventService.findByIdAndUser(1L, testUser)).thenReturn(testEvent); // PUBLISHED

        mockMvc.perform(patch("/users/1/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateEventUserDto)))
                .andExpect(status().isConflict());

        Mockito.verify(eventService, Mockito.never()).update(Mockito.any());
    }

    @Test
    @DisplayName("PATCH /admin/events/{eventId} - должен вернуть ошибку при невалидных данных")
    void review_ByAdmin_ShouldReturnErrorWhenInvalidData() throws Exception {
        UpdateEventAdminDto invalidDto = UpdateEventAdminDto.builder()
                .title("")  // пустой заголовок
                .build();

        mockMvc.perform(patch("/admin/events/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(eventService, Mockito.never()).findById(Mockito.anyLong());
    }

    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке")
    void shouldReturn500OnUnexpectedError() throws Exception {
        Mockito.when(userService.findById(1L)).thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/users/1/events"))
                .andExpect(status().isInternalServerError());
    }
}
