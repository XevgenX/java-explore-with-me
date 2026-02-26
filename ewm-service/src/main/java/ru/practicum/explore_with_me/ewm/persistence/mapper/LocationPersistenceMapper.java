package ru.practicum.explore_with_me.ewm.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewLocation;
import ru.practicum.explore_with_me.ewm.persistence.entity.LocationEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class LocationPersistenceMapper {

    public Location toDomain(LocationEntity entity) {
        if (entity == null) {
            return null;
        }

        return Location.builder()
                .id(entity.getId())
                .lat(entity.getLat())
                .lon(entity.getLon())
                .build();
    }

    public List<Location> toDomain(List<LocationEntity> entities) {
        if (entities == null) {
            return List.of();
        }
        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public LocationEntity toEntity(Location domain) {
        if (domain == null) {
            return null;
        }

        return LocationEntity.builder()
                .id(domain.getId())
                .lat(domain.getLat())
                .lon(domain.getLon())
                .build();
    }

    public void updateEntityFromDomain(Location domain, LocationEntity entity) {
        if (domain == null || entity == null) {
            return;
        }

        if (Objects.nonNull(domain.getLat())) {
            entity.setLat(domain.getLat());
        }
        if (Objects.nonNull(domain.getLon())) {
            entity.setLon(domain.getLon());
        }
    }

    public LocationEntity toNewEntity(NewLocation domain) {
        if (domain == null) {
            return null;
        }

        return LocationEntity.builder()
                .lat(domain.getLat())
                .lon(domain.getLon())
                .build();
    }
}
