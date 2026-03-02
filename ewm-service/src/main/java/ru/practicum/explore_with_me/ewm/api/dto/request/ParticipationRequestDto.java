package ru.practicum.explore_with_me.ewm.api.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipationRequestDto {
    private Long id;

    @NotNull(message = "Идентификатор события должен быть указан")
    private Long event;

    @NotNull(message = "Идентификатор пользователя должен быть указан")
    private Long requester;

    @NotNull(message = "Статус заявки должен быть указан")
    private RequestStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    private LocalDateTime created;
}
