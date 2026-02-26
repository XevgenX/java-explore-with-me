package ru.practicum.explore_with_me.common.domain.validator;

import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.util.Objects;

public interface TextValidatable {
    default void validate(String text, String errorMessage) {
        if (Objects.isNull(text) || text.isBlank()) {
            throw new ValidationException(errorMessage);
        }
    }
}
