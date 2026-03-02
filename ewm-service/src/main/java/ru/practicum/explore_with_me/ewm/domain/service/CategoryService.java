package ru.practicum.explore_with_me.ewm.domain.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.validator.IdValidatable;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;
import ru.practicum.explore_with_me.ewm.domain.repo.CategoryRepo;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CategoryService implements IdValidatable, ObjectValidatable {
    private final CategoryRepo repo;

    public List<Category> findAll(Integer from, Integer size) {
        return repo.findAll(from, size);
    }

    public Category findById(Long id) {
        validate(id, "id не должен быть null или отрицательным");
        return repo.findById(id);
    }

    public Category create(NewCategory category) {
        validate(category, "category не должен быть null");
        return repo.create(category);
    }

    public Category update(Category category) {
        validate(category, "category не должен быть null");
        validate(category.getId(), "id не должен быть null или отрицательным");
        return repo.update(category);
    }

    public void delete(Long id) {
        validate(id, "id не должен быть null или отрицательным");
        repo.delete(id);
    }
}
