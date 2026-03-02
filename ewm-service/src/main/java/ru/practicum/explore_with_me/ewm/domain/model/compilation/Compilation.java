package ru.practicum.explore_with_me.ewm.domain.model.compilation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;

import java.util.List;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class Compilation implements IdValidatable, ObjectValidatable {
    private Long id;
    private String title;
    private Boolean pined;
    private List<Event> events;

    public Compilation(Long id, String title, Boolean pined, List<Event> events) {
        validate(id, "У подборки должен корректным быть id");
        validate(title, "У подборки должно быть корректным title");
        validate(pined, "У подборки должно быть корректным pined");
        validate(events, "У подборки должно быть корректным events");
        this.id = id;
        this.title = title;
        this.pined = pined;
        this.events = events;
    }
}
