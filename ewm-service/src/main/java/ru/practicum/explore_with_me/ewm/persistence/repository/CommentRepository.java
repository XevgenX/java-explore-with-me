package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.repository.CrudRepository;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.persistence.entity.CommentEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.UserEntity;

import java.util.List;

public interface CommentRepository extends CrudRepository<CommentEntity, Long> {
    List<CommentEntity> findByEvent(EventEntity event);

    List<CommentEntity> findByEventAndStatus(EventEntity event, CommentStatus status);

    List<CommentEntity> findByAuthor(UserEntity author);
}
