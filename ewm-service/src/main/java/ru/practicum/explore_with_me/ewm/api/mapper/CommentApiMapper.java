package ru.practicum.explore_with_me.ewm.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.comment.CommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.EditCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.ModerationCommentDto;
import ru.practicum.explore_with_me.ewm.api.dto.comment.NewCommentDto;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Component
public class CommentApiMapper {
    private final EventApiMapper eventManager;
    private final UserApiMapper userMapper;

    public NewComment toDomain(NewCommentDto dto, User author, Event event) {
        return NewComment.builder()
                .text(dto.getText())
                .event(event)
                .author(author)
                .createdOn(LocalDateTime.now())
                .build();
    }

    public Comment toDomain(Comment existedComment, EditCommentDto dto) {
        existedComment.setText(dto.getText());
        existedComment.setUpdatedOn(LocalDateTime.now());
        existedComment.setStatus(CommentStatus.PENDING);
        return existedComment;
    }

    public CommentDto toDto(Comment model) {
        return CommentDto.builder()
                .id(model.getId())
                .text(model.getText())
                .event(eventManager.toShortDto(model.getEvent()))
                .author(userMapper.toDto(model.getAuthor()))
                .createdOn(model.getCreatedOn())
                .updatedOn(model.getUpdatedOn())
                .status(model.getStatus())
                .build();
    }

    public List<CommentDto> toDtos(List<Comment> comments) {
        return comments.stream().map(this::toDto).toList();
    }

    public CommentStatus toDomain(ModerationCommentDto.RequestStatusUpdate status) {
        return switch (status) {
            case APPROVED -> CommentStatus.APPROVED;
            case REJECTED -> CommentStatus.REJECTED;
            default -> CommentStatus.PENDING;
        };
    }
}
