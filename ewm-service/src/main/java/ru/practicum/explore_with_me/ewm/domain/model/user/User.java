package ru.practicum.explore_with_me.ewm.domain.model.user;

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
public class User implements IdValidatable, TextValidatable  {
    private Long id;
    private String name;
    private String email;

    public User(Long id, String name, String email) {
        validate(id, "У пользователя должен корректным быть id");
        validate(name, "У пользователя должно быть корректным имя");
        validate(email, "У пользователя должна быть корректной почта");
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
