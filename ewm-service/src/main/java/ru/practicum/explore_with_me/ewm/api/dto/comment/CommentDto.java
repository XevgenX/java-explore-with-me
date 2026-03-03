package ru.practicum.explore_with_me.ewm.api.dto.comment;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventShortDto;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;

import java.time.LocalDateTime;

@Schema(description = "Сохраненный комментарий")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {
    @Schema(description = "Идентификатор комментария", example = "123")
    private Long id;

    @Schema(description = "Сам комментарий", example = "Вау! Обязательно пойду!")
    private String text;

    @Schema(description = "Событие")
    private EventShortDto event;

    @Schema(description = "Автор")
    private UserDto author;

    @Schema(description = "Когда был создан комментарий")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime createdOn;

    @Schema(description = "Дата обновления комментария, если он обновлялся")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime updatedOn;

    @Schema(description = "Статус комментария")
    private CommentStatus status;
}
