package ru.practicum.explore_with_me.ewm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.ewm.api.dto.user.NewUserDto;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;
import ru.practicum.explore_with_me.ewm.api.mapper.UserApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping(path = "/admin/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final UserApiMapper mapper;

    @Operation(summary = "Получение информации о пользователях",
            description = "Возвращает информацию обо всех пользователях (учитываются параметры ограничения выборки), либо о конкретных (учитываются указанные идентификаторы)\\n\\nВ случае, если по заданным фильтрам не найдено ни одного пользователя, возвращает пустой список",
            tags = "Admin: Пользователи")
    @GetMapping
    public ResponseEntity<List<UserDto>> findById(@Parameter(description = "id пользователей", required = false)
                                                      @RequestParam(value = "ids", required = false)
                                                      List<Long> ids,
                                                  @Parameter(description = "количество элементов, которые нужно пропустить для формирования текущего набора", required = false)
                                                  @RequestParam(value = "from", defaultValue = "0", required = false)
                                                  Integer from,
                                                  @Parameter(description = "количество элементов в наборе", required = false)
                                                      @RequestParam(value = "size", defaultValue = "10", required = false)
                                                      Integer size,
                                                  HttpServletRequest request) {
        List<User> users = service.findByIds(ids, from, size);
        return ResponseEntity.ok(mapper.toDtos(users));
    }

    @Operation(summary = "Добавление нового пользователя",
            tags = "Admin: Пользователи")
    @PostMapping
    public ResponseEntity<UserDto> create(@RequestBody @Valid NewUserDto user,
                                          HttpServletRequest request) {
        User created = service.create(mapper.toModel(user));
        return ResponseEntity
                .created(URI.create("/users/" + created.getId()))
                .body(mapper.toDto(created));
    }

    @Operation(summary = "Удаление пользователя",
            tags = "Admin: Пользователи")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "id пользователя", required = true) @PathVariable long id,
                                           HttpServletRequest request) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
