package ru.practicum.explore_with_me.ewm.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewParticipationRequestDto {
    @NotNull(message = "Идентификатор события должен быть указан")
    private Long eventId;

    @NotNull(message = "Идентификатор пользователя должен быть указан")
    private Long userId;
}
