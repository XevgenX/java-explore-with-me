package ru.practicum.explore_with_me.ewm.api.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Данные модерации")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModerationCommentDto {
    @Schema(description = "Идентификаторы комментариев")
    @NotNull(message = "Список идентификаторов комментариев должен быть указан")
    private List<Long> requestIds;

    @Schema(description = "Статус модерации")
    @NotNull(message = "Статус должен быть указан")
    private RequestStatusUpdate status;

    public enum RequestStatusUpdate {
        APPROVED,
        REJECTED
    }
}
