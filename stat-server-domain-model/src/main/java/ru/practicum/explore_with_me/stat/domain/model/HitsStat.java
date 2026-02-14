package ru.practicum.explore_with_me.stat.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class HitsStat implements TextValidatable {
    private final String server;
    private final String uri;
    private final Integer hits;

    public HitsStat(String server, String uri, Integer hits) {
        validate(server, "У запроса должен быть корректным сервер");
        validate(uri, "У запроса должен быть корректным URI");
        this.server = server;
        this.uri = uri;
        this.hits = hits;
    }
}
