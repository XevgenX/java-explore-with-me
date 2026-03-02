package ru.practicum.explore_with_me.ewm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.common.domain.exception.ForbiddenException;
import ru.practicum.explore_with_me.ewm.api.dto.comment.CommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.EditCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.ModerationCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.NewCommentDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CommentApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.CommentService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final EventService eventService;
    private final UserService userService;
    private final CommentApiMapper mapper;

    @Operation(summary = "Получение комментариев для события",
            description = "В выборку попадают только комментарии, которые успешно прошли модерацию",
            tags = "Public: События")
    @GetMapping("/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> findCommentsForEventForShow(@Parameter(description = "id события", required = true)
                                                                     @PathVariable("eventId")
                                                                     long eventId,
                                                                 HttpServletRequest request) {
        Event event = eventService.findById(eventId);
        return ResponseEntity.ok(mapper.toDtos(commentService.findApprovedEvent(event)));
    }

    @Operation(summary = "Получение комментариев на событие для модерации",
            tags = "Private: События")
    @GetMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> findCommentsForModeration(@Parameter(description = "id текущего пользователя", required = true)
                                                                          @PathVariable
                                                                          Long userId,
                                                                      @Parameter(description = "id события", required = true)
                                                                          @PathVariable
                                                                          Long eventId,
                                                                      HttpServletRequest request) {
        User initiator = userService.findById(userId);
        Event event = eventService.findByIdAndUser(eventId, initiator);
        return ResponseEntity.ok(mapper.toDtos(commentService.findAllByEvent(event)));
    }

    @Operation(summary = "Получение списка своих комментариев",
            tags = "Private: Комментарии")
    @GetMapping("/users/{userId}/comments")
    public ResponseEntity<List<CommentDto>> findUsersComments(@Parameter(description = "id текущего пользователя", required = true)
                                                                      @PathVariable
                                                                      Long userId,
                                                                      HttpServletRequest request) {
        User author = userService.findById(userId);
        return ResponseEntity.ok(mapper.toDtos(commentService.findByUser(author)));
    }

    @Operation(summary = "Добавление комментария от текущего пользователя на событие",
            description = "Новый комментарий должен будет пройти модерацию прежде чем станет доступным",
            tags = "Private: Комментарии")
    @PostMapping("/users/{userId}/comments")
    public ResponseEntity<CommentDto> create(@Parameter(description = "id текущего пользователя", required = true)
                                                 @PathVariable
                                                 Long userId,
                                             @RequestBody(required = false)
                                             @Valid
                                                 NewCommentDto dto,
                                             HttpServletRequest request) {
        User author = userService.findById(userId);
        Event event = eventService.findById(dto.getEventId());
        NewComment comment = mapper.toDomain(dto, author, event);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDto(commentService.create(comment)));
    }

    @Operation(summary = "Обновление комментария от текущего пользователя на событие",
            description = "Обновленный комментарий должен будет пройти модерацию прежде чем станет доступным",
            tags = "Private: Комментарии")
    @PatchMapping("/users/{userId}/comments/{id}")
    public ResponseEntity<CommentDto> update(@Parameter(description = "id текущего пользователя", required = true)
                                             @PathVariable
                                             Long userId,
                                             @Parameter(description = "id комментария", required = true)
                                                 @PathVariable
                                                 long id,
                                             @RequestBody(required = false)
                                             @Valid
                                                 EditCommentDto dto,
                                             HttpServletRequest request) {
        User author = userService.findById(userId);
        Comment existedComment = commentService.findById(id);
        if (!existedComment.getAuthor().getId().equals(author.getId())) {
            throw new ForbiddenException("Вы не являетесь автором комментария");
        }
        Comment comment = mapper.toDomain(existedComment, dto);
        return ResponseEntity
                .ok(mapper.toDto(commentService.update(comment)));
    }

    @Operation(summary = "Удаление комментария от текущего пользователя на событие",
            tags = "Private: Комментарии")
    @DeleteMapping("/users/{userId}/comments/{id}")
    public ResponseEntity<Void> delete(@Parameter(description = "id текущего пользователя", required = true)
                                           @PathVariable
                                           Long userId,
                                       @Parameter(description = "id комментария", required = true)
                                           @PathVariable
                                           long id,
                                       HttpServletRequest request) {
        User author = userService.findById(userId);
        Comment existedComment = commentService.findById(id);
        if (!existedComment.getAuthor().getId().equals(author.getId())) {
            throw new ForbiddenException("Вы не являетесь автором комментария");
        }
        commentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Модерация комментариев на событие",
            tags = "Private: События")
    @PatchMapping("/users/{userId}/events/{eventId}/comments")
    public ResponseEntity<List<CommentDto>> moderation(@Parameter(description = "id текущего пользователя", required = true)
                                                                      @PathVariable
                                                                      Long userId,
                                                                      @Parameter(description = "id события", required = true)
                                                                      @PathVariable
                                                                      Long eventId,
                                                       @RequestBody(required = false)
                                                           @Valid
                                                           ModerationCommentDto dto,
                                                                      HttpServletRequest request) {
        User initiator = userService.findById(userId);
        Event event = eventService.findByIdAndUser(eventId, initiator);
        dto.getRequestIds().forEach(commentId -> {
            Comment comment = commentService.findById(commentId);
            comment.setStatus(mapper.toDomain(dto.getStatus()));
            commentService.update(comment);
        });
        return ResponseEntity.ok(mapper.toDtos(commentService.findAllByEvent(event)));
    }
}
