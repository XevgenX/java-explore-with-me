package ru.practicum.explore_with_me.ewm.domain.model.compilation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;

import java.util.List;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class NewCompilation implements ObjectValidatable {
    private final String title;
    private final Boolean pined;
    private final List<Event> events;

    public NewCompilation(String title, Boolean pined, List<Event> events) {
        validate(title, "У подборки должно быть корректным title");
        validate(pined, "У подборки должно быть корректным pined");
        validate(events, "У подборки должно быть корректным events");
        this.title = title;
        this.pined = pined;
        this.events = events;
    }
}
