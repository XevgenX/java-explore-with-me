package ru.practicum.explore_with_me.ewm.api.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Обновление комментария")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EditCommentDto {

    @Schema(description = "Новый текст комментария", example = "Вау! Обязательно пойду!")
    @NotBlank
    @Size(max = 250)
    private String text;
}
