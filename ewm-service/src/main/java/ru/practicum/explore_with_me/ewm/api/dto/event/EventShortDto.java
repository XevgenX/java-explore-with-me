package ru.practicum.explore_with_me.ewm.api.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.practicum.explore_with_me.ewm.api.dto.category.CategoryDto;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;

import java.time.LocalDateTime;

@Value
@Builder
@Jacksonized
public class EventShortDto {

    Long id;

    String annotation;

    CategoryDto category;

    Long confirmedRequests;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime eventDate;

    UserDto initiator;

    Boolean paid;

    String title;

    Long views;
}
