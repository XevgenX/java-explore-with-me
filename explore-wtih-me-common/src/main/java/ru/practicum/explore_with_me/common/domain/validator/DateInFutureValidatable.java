package ru.practicum.explore_with_me.common.domain.validator;

import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.time.LocalDateTime;
import java.util.Objects;

public interface DateInFutureValidatable {
    default void validate(LocalDateTime text, String errorMessage) {
        if (Objects.isNull(text)
                || text.isBefore(LocalDateTime.now())) {
            throw new ValidationException(errorMessage);
        }
    }
}
