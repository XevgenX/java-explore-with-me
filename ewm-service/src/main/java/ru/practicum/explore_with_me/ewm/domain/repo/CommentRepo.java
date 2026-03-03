package ru.practicum.explore_with_me.ewm.domain.repo;

import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.util.List;

public interface CommentRepo {
    List<Comment> findAllByEvent(Event event);

    List<Comment> findApprovedEvent(Event event);

    List<Comment> findByUser(User user);

    Comment create(NewComment comment);

    Comment update(Comment comment);

    Comment findById(Long id);

    void delete(Long id);
}
