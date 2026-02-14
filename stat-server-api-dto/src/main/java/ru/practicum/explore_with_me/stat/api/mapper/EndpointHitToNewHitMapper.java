package ru.practicum.explore_with_me.stat.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;

@Component
public class EndpointHitToNewHitMapper {
    public NewHit convert(EndpointHit source) {
        if (source == null) {
            return null;
        }

        return NewHit.builder()
                .server(source.getApp())
                .uri(source.getUri())
                .ip(source.getIp())
                .time(source.getTimestamp())
                .build();
    }
}
