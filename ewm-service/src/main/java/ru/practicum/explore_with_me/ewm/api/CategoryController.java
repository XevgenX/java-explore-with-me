package ru.practicum.explore_with_me.ewm.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.ewm.api.dto.category.CategoryDto;
import ru.practicum.explore_with_me.ewm.api.dto.category.NewCategoryDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CategoryApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.service.CategoryService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService service;
    private final CategoryApiMapper mapper;

    @Operation(summary = "Получение категорий",
            description = "В случае, если по заданным фильтрам не найдено ни одной категории, возвращает пустой список",
            tags = "Public: Категории")
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> findAll(@Parameter(description = "количество категорий, которые нужно пропустить для формирования текущего набора", required = false)
                                                         @RequestParam(value = "from", defaultValue = "0", required = false)
                                                         Integer from,
                                                     @Parameter(description = "количество категорий в наборе", required = false)
                                                     @RequestParam(value = "size", defaultValue = "10", required = false)
                                                     Integer size,
                                                     HttpServletRequest request) {
        List<Category> categories = service.findAll(from, size);
        return ResponseEntity.ok(mapper.toDtos(categories));
    }

    @Operation(summary = "Получение информации о категории по её идентификатору",
            description = "В случае, если категории с заданным id не найдено, возвращает статус код 404",
            tags = "Public: Категории")
    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDto> findById(@PathVariable long id) {
        return ResponseEntity.ok(mapper.toDto(service.findById(id)));
    }

    @Operation(summary = "Добавление новой категории",
            description = "Обратите внимание: имя категории должно быть уникальным",
            tags = "Admin: Категории")
    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDto> create(@RequestBody @Valid NewCategoryDto user,
                                              HttpServletRequest request) {
        Category created = service.create(mapper.toModel(user));
        return ResponseEntity
                .created(URI.create("/categories/" + created.getId()))
                .body(mapper.toDto(created));
    }

    @Operation(summary = "Изменение категории",
            description = "Обратите внимание: имя категории должно быть уникальным",
            tags = "Admin: Категории")
    @PatchMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDto> update(@Parameter(description = "id категории", required = true) @PathVariable long id,
                                              @RequestBody @Valid NewCategoryDto dto,
                                              HttpServletRequest request) {
        Category existedCategory = service.findById(id);
        if (!existedCategory.getName().equals(dto.getName())) {
            existedCategory.setName(dto.getName());
        }
        Category created = service.update(existedCategory);
        return ResponseEntity.ok(mapper.toDto(existedCategory));
    }

    @Operation(summary = "Удаление категории",
            description = "Обратите внимание: с категорией не должно быть связано ни одного события.",
            tags = "Admin: Категории")
    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<Void> deleteById(@Parameter(description = "id категории", required = true) @PathVariable long id,
                                           HttpServletRequest request) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
