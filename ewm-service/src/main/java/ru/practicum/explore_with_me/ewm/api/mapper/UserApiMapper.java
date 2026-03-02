package ru.practicum.explore_with_me.ewm.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.user.NewUserDto;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserApiMapper {
    public User toModel(UserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return User.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public NewUser toModel(NewUserDto userDto) {
        if (userDto == null) {
            return null;
        }

        return NewUser.builder()
                .name(userDto.getName())
                .email(userDto.getEmail())
                .build();
    }

    public List<User> toModels(List<UserDto> userDtos) {
        if (userDtos == null) {
            return null;
        }

        return userDtos.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public UserDto toDto(User user) {
        if (user == null) {
            return null;
        }

        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .build();
    }

    public List<UserDto> toDtos(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
