package ru.practicum.explore_with_me.ewm.api.dto.event;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import jakarta.validation.constraints.NotNull;

@Value
@Builder
@Jacksonized
public class LocationDto {

    @NotNull
    Float lat;

    @NotNull
    Float lon;
}
