package ru.practicum.explore_with_me.stat.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class Hit implements IdValidatable, TextValidatable, ObjectValidatable {
    private final Long id;
    private final String server;
    private final String uri;
    private final String ip;
    private final LocalDateTime time;

    public Hit(Long id, String server, String uri, String ip, LocalDateTime time) {
        validate(id, "У запроса должен корректным быть id");
        validate(server, "У запроса должен быть корректным URI");
        validate(uri, "У запроса должен быть корректным URI");
        validate(ip, "У запроса должен корректным быть IP");
        this.id = id;
        this.server = server;
        this.uri = uri;
        this.ip = ip;
        this.time = time;
    }

    public Hit(Long id, NewHit newHit) {
        this(id, newHit.getServer(), newHit.getUri(), newHit.getIp(), newHit.getTime());
    }
}
