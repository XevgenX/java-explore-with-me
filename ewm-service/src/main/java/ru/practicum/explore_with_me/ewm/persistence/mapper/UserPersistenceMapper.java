package ru.practicum.explore_with_me.ewm.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.persistence.entity.UserEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class UserPersistenceMapper {
    public List<User> toDomain(List<UserEntity> entities) {
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    public User toDomain(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        return User.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .build();
    }

    public UserEntity toEntity(User domain) {
        if (domain == null) {
            return null;
        }

        return UserEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .email(domain.getEmail())
                .build();
    }

    public void updateEntityFromDomain(User domain, UserEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        if (Objects.nonNull(domain.getName()) && !domain.getName().isBlank()) {
            entity.setName(domain.getName());
        }
        if (Objects.nonNull(domain.getEmail()) && !domain.getEmail().isBlank()) {
            entity.setEmail(domain.getEmail());
        }
    }

    public UserEntity toNewEntity(NewUser domain) {
        if (domain == null) {
            return null;
        }

        return UserEntity.builder()
                .name(domain.getName())
                .email(domain.getEmail())
                .build();
    }
}
