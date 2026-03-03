package ru.practicum.explore_with_me.ewm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.api.dto.comment.CommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.EditCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.ModerationCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.NewCommentDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CommentApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.CommentService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.explore_with_me.ewm.api.dto.comment.ModerationCommentDto.RequestStatusUpdate.APPROVED;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера комментариев")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserService userService;

    @Autowired
    private CommentApiMapper mapper;

    private User testUser;
    private User testAuthor;
    private User testInitiator;
    private Event testEvent;
    private Comment testComment;
    private CommentDto testCommentDto;
    private NewComment testNewComment;
    private NewCommentDto testNewCommentDto;
    private EditCommentDto testEditCommentDto;
    private ModerationCommentDto testModerationDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Тестовый Пользователь")
                .email("test@example.com")
                .build();

        testAuthor = User.builder()
                .id(2L)
                .name("Автор Комментария")
                .email("author@example.com")
                .build();

        testInitiator = User.builder()
                .id(3L)
                .name("Инициатор События")
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

        testComment = Comment.builder()
                .id(100L)
                .text("Тестовый комментарий")
                .author(testAuthor)
                .event(testEvent)
                .status(CommentStatus.PENDING)
                .createdOn(LocalDateTime.now())
                .build();

        testCommentDto = mapper.toDto(testComment);

        testNewCommentDto = NewCommentDto.builder()
                .eventId(10L)
                .text("Новый тестовый комментарий")
                .build();

        testNewComment = NewComment.builder()
                .event(testEvent)
                .author(testAuthor)
                .text("Новый тестовый комментарий")
                .createdOn(LocalDateTime.now())
                .build();

        testEditCommentDto = EditCommentDto.builder()
                .text("Обновленный текст комментария")
                .build();

        testModerationDto = ModerationCommentDto.builder()
                .requestIds(List.of(100L, 101L))
                .status(APPROVED)
                .build();
    }

    @Test
    @DisplayName("Должен вернуть список комментариев для события (публичный доступ)")
    void shouldReturnCommentsForEvent() throws Exception {
        List<Comment> comments = List.of(testComment);
        List<CommentDto> commentDtos = List.of(testCommentDto);

        when(eventService.findById(10L)).thenReturn(testEvent);
        when(commentService.findApprovedEvent(testEvent)).thenReturn(comments);

        mockMvc.perform(get("/events/10/comments")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(100)))
                .andExpect(jsonPath("$[0].text", is("Тестовый комментарий")));

        verify(eventService, times(1)).findById(10L);
        verify(commentService, times(1)).findApprovedEvent(testEvent);
    }

    @Test
    @DisplayName("Должен вернуть пустой список комментариев для события, если нет одобренных")
    void shouldReturnEmptyListWhenNoApprovedComments() throws Exception {
        when(eventService.findById(10L)).thenReturn(testEvent);
        when(commentService.findApprovedEvent(testEvent)).thenReturn(List.of());

        mockMvc.perform(get("/events/10/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(eventService, times(1)).findById(10L);
        verify(commentService, times(1)).findApprovedEvent(testEvent);
    }

    @Test
    @DisplayName("Должен вернуть 404 при запросе комментариев для несуществующего события")
    void shouldReturn404WhenEventNotFoundForComments() throws Exception {
        when(eventService.findById(999L)).thenThrow(new NotFoundException("Event not found"));

        mockMvc.perform(get("/events/999/comments"))
                .andExpect(status().isNotFound());

        verify(eventService, times(1)).findById(999L);
        verify(commentService, never()).findApprovedEvent(any());
    }

    @Test
    @DisplayName("Должен вернуть 409 при попытке модерации не своим событием")
    void shouldReturn403WhenNotEventOwnerForModeration() throws Exception {
        when(userService.findById(2L)).thenReturn(testAuthor);
        when(eventService.findByIdAndUser(10L, testAuthor))
                .thenThrow(new ForbiddenException("Вы не являетесь инициатором события"));

        mockMvc.perform(get("/users/2/events/10/comments"))
                .andExpect(status().isConflict());

        verify(userService, times(1)).findById(2L);
        verify(eventService, times(1)).findByIdAndUser(10L, testAuthor);
        verify(commentService, never()).findAllByEvent(any());
    }

    @Test
    @DisplayName("Должен вернуть список комментариев текущего пользователя")
    void shouldReturnUsersComments() throws Exception {
        List<Comment> comments = List.of(testComment);
        List<CommentDto> commentDtos = List.of(testCommentDto);

        when(userService.findById(2L)).thenReturn(testAuthor);
        when(commentService.findByUser(testAuthor)).thenReturn(comments);

        mockMvc.perform(get("/users/2/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].author.id", is(2)));

        verify(userService, times(1)).findById(2L);
        verify(commentService, times(1)).findByUser(testAuthor);
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если у пользователя нет комментариев")
    void shouldReturnEmptyListWhenUserHasNoComments() throws Exception {
        when(userService.findById(2L)).thenReturn(testAuthor);
        when(commentService.findByUser(testAuthor)).thenReturn(List.of());

        mockMvc.perform(get("/users/2/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(userService, times(1)).findById(2L);
        verify(commentService, times(1)).findByUser(testAuthor);
    }

    @Test
    @DisplayName("Должен вернуть ошибку при создании комментария с пустым текстом")
    void shouldReturnErrorWhenCommentTextIsBlank() throws Exception {
        NewCommentDto invalidDto = NewCommentDto.builder()
                .eventId(10L)
                .text("")
                .build();

        mockMvc.perform(post("/users/2/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(any());
        verify(eventService, never()).findById(any());
        verify(commentService, never()).create(any());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при создании комментария без eventId")
    void shouldReturnErrorWhenEventIdIsNull() throws Exception {
        NewCommentDto invalidDto = NewCommentDto.builder()
                .text("Текст комментария")
                .build();

        mockMvc.perform(post("/users/2/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть 409 при обновлении чужого комментария")
    void shouldReturn403WhenUpdatingOthersComment() throws Exception {
        when(userService.findById(1L)).thenReturn(testUser);
        when(commentService.findById(100L)).thenReturn(testComment);

        mockMvc.perform(patch("/users/1/comments/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEditCommentDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).findById(1L);
        verify(commentService, times(1)).findById(100L);
        verify(commentService, never()).update(any());
    }

    @Test
    @DisplayName("Должен вернуть 404 при обновлении несуществующего комментария")
    void shouldReturn404WhenUpdatingNonExistingComment() throws Exception {
        when(userService.findById(2L)).thenReturn(testAuthor);
        when(commentService.findById(999L)).thenThrow(new NotFoundException("Comment not found"));

        mockMvc.perform(patch("/users/2/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEditCommentDto)))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).findById(2L);
        verify(commentService, times(1)).findById(999L);
        verify(commentService, never()).update(any());
    }

    @Test
    @DisplayName("Должен удалить комментарий")
    void shouldDeleteComment() throws Exception {
        when(userService.findById(2L)).thenReturn(testAuthor);
        when(commentService.findById(100L)).thenReturn(testComment);
        doNothing().when(commentService).delete(100L);

        mockMvc.perform(delete("/users/2/comments/100"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).findById(2L);
        verify(commentService, times(1)).findById(100L);
        verify(commentService, times(1)).delete(100L);
    }

    @Test
    @DisplayName("Должен вернуть 409 при удалении чужого комментария")
    void shouldReturn403WhenDeletingOthersComment() throws Exception {
        when(userService.findById(1L)).thenReturn(testUser);
        when(commentService.findById(100L)).thenReturn(testComment);

        mockMvc.perform(delete("/users/1/comments/100"))
                .andExpect(status().isConflict());

        verify(userService, times(1)).findById(1L);
        verify(commentService, times(1)).findById(100L);
        verify(commentService, never()).delete(any());
    }

    @Test
    @DisplayName("Должен вернуть 409 при модерации не своим событием")
    void shouldReturn403WhenModeratingNotOwnEvent() throws Exception {
        when(userService.findById(2L)).thenReturn(testAuthor);
        when(eventService.findByIdAndUser(10L, testAuthor))
                .thenThrow(new ForbiddenException("Вы не являетесь инициатором события"));

        mockMvc.perform(patch("/users/2/events/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testModerationDto)))
                .andExpect(status().isConflict());

        verify(userService, times(1)).findById(2L);
        verify(eventService, times(1)).findByIdAndUser(10L, testAuthor);
        verify(commentService, never()).findById(any());
        verify(commentService, never()).update(any());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при модерации без списка requestIds")
    void shouldReturnErrorWhenModerationWithoutRequestIds() throws Exception {
        ModerationCommentDto invalidDto = ModerationCommentDto.builder()
                .status(APPROVED)
                .build();

        mockMvc.perform(patch("/users/3/events/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).findById(any());
        verify(eventService, never()).findByIdAndUser(any(), any());
        verify(commentService, never()).update(any());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при модерации без статуса")
    void shouldReturnErrorWhenModerationWithoutStatus() throws Exception {
        ModerationCommentDto invalidDto = ModerationCommentDto.builder()
                .requestIds(List.of(100L))
                .build();

        mockMvc.perform(patch("/users/3/events/10/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}
