package ru.practicum.explore_with_me.ewm.domain.model.compilation;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;

import java.util.List;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class UpdatingCompilation {
    private Long id;
    private String title;
    private Boolean pined;
    private List<Event> events;

    public UpdatingCompilation(Long id, String title, Boolean pined, List<Event> events) {
        this.id = id;
        this.title = title;
        this.pined = pined;
        this.events = events;
    }
}
