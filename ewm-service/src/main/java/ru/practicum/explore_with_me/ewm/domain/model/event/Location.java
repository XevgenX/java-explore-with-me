package ru.practicum.explore_with_me.ewm.domain.model.event;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class Location implements ObjectValidatable {
    private Long id;
    private Float lat;
    private Float lon;

    public Location(Long id, Float lat, Float lon) {
        validate(lat, "В месторасположении должен быть корректным lat");
        validate(lon, "В месторасположении должен быть корректным lon");
        this.lat = lat;
        this.lon = lon;
    }
}
