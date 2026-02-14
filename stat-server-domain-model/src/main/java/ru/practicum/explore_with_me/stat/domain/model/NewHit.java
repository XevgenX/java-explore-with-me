package ru.practicum.explore_with_me.stat.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.NowDateValidatable;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

import java.time.LocalDateTime;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class NewHit implements TextValidatable, NowDateValidatable {
    private final String server;
    private final String uri;
    private final String ip;
    private final LocalDateTime time;

    public NewHit(String server, String uri, String ip, LocalDateTime time) {
        validate(server, "У запроса должен быть корректным URI");
        validate(uri, "У запроса должен быть корректным URI");
        validate(ip, "У запроса должен корректным быть IP");
        validate(time, "У запроса должно быть корректное время");
        this.server = server;
        this.uri = uri;
        this.ip = ip;
        this.time = time;
    }
}
