package ru.practicum.explore_with_me.common.domain.validator;

import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.util.Objects;

public interface IdValidatable {
    default void validate(Long id, String errorMessage) {
        if (Objects.isNull(id) || id < 0) {
            throw new ValidationException(errorMessage);
        }
    }
}
