package ru.practicum.explore_with_me.common.domain.validator;

import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.util.Objects;

public interface ObjectValidatable {
    default void validate(Object object, String errorMessage) {
        if (Objects.isNull(object)) {
            throw new ValidationException(errorMessage);
        }
    }
}
