package ru.practicum.explore_with_me.ewm.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.persistence.entity.CommentEntity;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CommentPersistenceMapper {
    private final EventPersistenceMapper eventMapper;
    private final UserPersistenceMapper userMapper;

    public CommentEntity toEntity(Comment comment) {
        if (comment == null) {
            return null;
        }

        return CommentEntity.builder()
                .id(comment.getId())
                .text(comment.getText())
                .event(eventMapper.toEntity(comment.getEvent()))
                .author(userMapper.toEntity(comment.getAuthor()))
                .createdOn(comment.getCreatedOn())
                .updatedOn(comment.getUpdatedOn())
                .status(comment.getStatus())
                .build();
    }

    public Comment toModel(CommentEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Comment(
                entity.getId(),
                entity.getText(),
                eventMapper.toDomain(entity.getEvent()),
                userMapper.toDomain(entity.getAuthor()),
                entity.getCreatedOn(),
                entity.getUpdatedOn(),
                entity.getStatus()
        );
    }

    public List<Comment> toModels(List<CommentEntity> entities) {
        return entities.stream().map(this::toModel).collect(Collectors.toList());
    }

    public CommentEntity toEntity(NewComment newComment) {
        if (newComment == null) {
            return null;
        }

        return CommentEntity.builder()
                .text(newComment.getText())
                .event(eventMapper.toEntity(newComment.getEvent()))
                .author(userMapper.toEntity(newComment.getAuthor()))
                .createdOn(newComment.getCreatedOn())
                .status(CommentStatus.PENDING)
                .build();
    }
}
