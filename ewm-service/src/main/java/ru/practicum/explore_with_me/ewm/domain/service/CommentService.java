package ru.practicum.explore_with_me.ewm.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.CommentRepo;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CommentService implements ObjectValidatable {
    private final CommentRepo repo;

    public List<Comment> findAllByEvent(Event event) {
        validate(event, "event не должен быть null");
        return repo.findAllByEvent(event);
    }

    public List<Comment> findApprovedEvent(Event event) {
        validate(event, "event не должен быть null");
        return repo.findApprovedEvent(event);
    }

    public List<Comment> findByUser(User user) {
        validate(user, "user не должен быть null");
        return repo.findByUser(user);
    }

    public Comment findById(Long id) {
        validate(id, "id не должен быть null");
        return repo.findById(id);
    }

    public Comment create(NewComment comment) {
        validate(comment, "comment не должен быть null");
        return repo.create(comment);
    }

    public Comment update(Comment comment) {
        validate(comment, "comment не должен быть null");
        return repo.update(comment);
    }

    public void delete(Long id) {
        validate(id, "id не должен быть null");
        repo.delete(id);
    }
}
