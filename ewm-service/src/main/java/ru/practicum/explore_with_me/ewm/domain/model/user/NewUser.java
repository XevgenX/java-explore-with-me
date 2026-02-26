package ru.practicum.explore_with_me.ewm.domain.model.user;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import ru.practicum.explore_with_me.common.domain.validator.TextValidatable;

@EqualsAndHashCode
@ToString
@Getter
@Builder
public class NewUser implements TextValidatable {
    private final String name;
    private final String email;

    public NewUser(String name, String email) {
        validate(name, "У пользователя должно быть корректным имя");
        validate(email, "У пользователя должна быть корректной почта");
        this.name = name;
        this.email = email;
    }
}
