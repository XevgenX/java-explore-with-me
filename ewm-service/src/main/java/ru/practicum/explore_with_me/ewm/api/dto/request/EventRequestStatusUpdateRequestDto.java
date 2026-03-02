package ru.practicum.explore_with_me.ewm.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequestStatusUpdateRequestDto {
    @NotNull(message = "Список идентификаторов запросов должен быть указан")
    private List<Long> requestIds;

    @NotNull(message = "Статус должен быть указан")
    private RequestStatusUpdate status;

    public enum RequestStatusUpdate {
        CONFIRMED,
        REJECTED
    }
}
