package ru.practicum.explore_with_me.common.domain.validator;

import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public interface NowDateValidatable {
    default void validate(LocalDateTime text, String errorMessage) {
        if (Objects.isNull(text)
                || text.isAfter(LocalDateTime.now())) {
            throw new ValidationException(errorMessage);
        }
    }
}
