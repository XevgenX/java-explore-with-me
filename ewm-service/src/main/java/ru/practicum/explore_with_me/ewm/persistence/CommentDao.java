package ru.practicum.explore_with_me.ewm.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.CommentRepo;
import ru.practicum.explore_with_me.ewm.persistence.mapper.CommentPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.mapper.EventPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.mapper.UserPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.CommentRepository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CommentDao implements CommentRepo {
    private final CommentRepository repository;
    private final CommentPersistenceMapper commentMapper;
    private final EventPersistenceMapper eventMapper;
    private final UserPersistenceMapper userMapper;

    @Override
    public List<Comment> findAllByEvent(Event event) {
        return commentMapper.toModels(repository.findByEvent(eventMapper.toEntity(event)));
    }

    @Override
    public List<Comment> findApprovedEvent(Event event) {
        return commentMapper.toModels(repository
                .findByEventAndStatus(eventMapper.toEntity(event), CommentStatus.APPROVED));
    }

    @Override
    public List<Comment> findByUser(User user) {
        return commentMapper.toModels(repository.findByAuthor(userMapper.toEntity(user)));
    }

    @Override
    public Comment findById(Long id) {
        return commentMapper.toModel(repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден")));
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public Comment create(NewComment comment) {
        return commentMapper.toModel(repository.save(commentMapper.toEntity(comment)));
    }

    @Override
    public Comment update(Comment comment) {
        return commentMapper.toModel(repository.save(commentMapper.toEntity(comment)));
    }
}
