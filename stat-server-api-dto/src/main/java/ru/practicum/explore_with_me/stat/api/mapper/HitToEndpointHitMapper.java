package ru.practicum.explore_with_me.stat.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.domain.model.Hit;

@Component
public class HitToEndpointHitMapper {
    public EndpointHit convert(Hit source) {
        if (source == null) {
            return null;
        }

        EndpointHit target = new EndpointHit();
        target.setId(source.getId());
        target.setApp(source.getServer());
        target.setUri(source.getUri());
        target.setIp(source.getIp());
        target.setTimestamp(source.getTime());

        return target;
    }
}