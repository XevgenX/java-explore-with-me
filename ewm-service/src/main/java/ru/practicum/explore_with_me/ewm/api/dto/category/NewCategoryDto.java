package ru.practicum.explore_with_me.ewm.api.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NewCategoryDto {
    @NotBlank
    @Size(max = 50)
    String name;
}
