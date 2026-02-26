package ru.practicum.explore_with_me.ewm.domain.repo;

import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.util.List;

public interface UserRepo {
    List<User> findByIds(List<Long> id, Integer from, Integer size);

    User findById(Long id);

    User create(NewUser user);

    void delete(Long id);
}
