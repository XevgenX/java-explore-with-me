package ru.practicum.explore_with_me.ewm.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.repo.UserRepo;
import ru.practicum.explore_with_me.ewm.persistence.mapper.UserPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserDao implements UserRepo {
    private final UserRepository repository;
    private final UserPersistenceMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<User> findByIds(List<Long> id, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        List<User> users = new ArrayList<>();
        repository.findAll(id, page).forEach(entity -> users.add(mapper.toDomain(entity)));
        return users;
    }

    @Transactional(readOnly = true)
    @Override
    public User findById(Long id) {
        return mapper.toDomain(repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден")));
    }

    @Transactional
    @Override
    public User create(NewUser user) {
        return mapper.toDomain(repository.save(mapper.toNewEntity(user)));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
