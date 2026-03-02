package ru.practicum.explore_with_me.ewm.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class UpdateEventUserDto {

    @Size(min = 20, max = 2000)
    String annotation;

    Long category;

    @Size(min = 20, max = 7000)
    String description;

    @Future
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    LocationDto location;

    Boolean paid;

    @PositiveOrZero
    Integer participantLimit;

    Boolean requestModeration;

    @Size(min = 3, max = 120)
    String title;

    UserStateAction stateAction;

    public enum UserStateAction {
        SEND_TO_REVIEW,
        CANCEL_REVIEW
    }
}
