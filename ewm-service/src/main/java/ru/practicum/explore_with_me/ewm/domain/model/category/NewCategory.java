package ru.practicum.explore_with_me.ewm.domain.model.category;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class NewCategory implements TextValidatable {
    private final String name;

    public NewCategory(String name) {
        validate(name, "У категории должно быть корректным имя");
        this.name = name;
    }
}
