package ru.practicum.explore_with_me.ewm.domain.model.category;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class Category implements IdValidatable, TextValidatable {
    private Long id;
    private String name;

    public Category(Long id, String name) {
        validate(id, "У категории должен корректным быть id");
        validate(name, "У категории должно быть корректным имя");
        this.id = id;
        this.name = name;
    }
}
