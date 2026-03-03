package ru.practicum.explore_with_me.ewm.api.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Новый комментарий")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewCommentDto {
    @Schema(description = "Идентификатор события", example = "123")
    @NotNull(message = "Идентификатор события должен быть указан")
    private Long eventId;

    @Schema(description = "Сам комментарий", example = "Вау! Обязательно пойду!")
    @NotBlank
    @Size(max = 250)
    private String text;
}
