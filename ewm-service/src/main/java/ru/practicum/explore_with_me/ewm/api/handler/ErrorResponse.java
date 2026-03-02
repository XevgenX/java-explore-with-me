package ru.practicum.explore_with_me.ewm.api.handler;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("error")
        String title,

        int status,

        @JsonProperty("description")
        String detail, String path) { }
