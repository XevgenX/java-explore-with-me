package ru.practicum.explore_with_me.ewm.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.UserRepo;

import java.util.List;

@RequiredArgsConstructor
@Service
public class UserService implements IdValidatable, ObjectValidatable {
    private final UserRepo repo;

    public List<User> findByIds(List<Long> ids, Integer from, Integer size) {
        return repo.findByIds(ids, from, size);
    }

    public User findById(Long id) {
        validate(id, "id не должен быть null");
        return repo.findById(id);
    }

    public User create(NewUser user) {
        validate(user, "user не должен быть null");
        return repo.create(user);
    }


    public void delete(Long id) {
        validate(id, "id не должен быть null или отрицательным");
        repo.delete(id);
    }
}
