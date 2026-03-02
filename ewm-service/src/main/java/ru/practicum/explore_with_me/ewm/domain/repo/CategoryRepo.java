package ru.practicum.explore_with_me.ewm.domain.repo;


import org.springframework.lang.Nullable;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;

import java.util.List;

public interface CategoryRepo {
    List<Category> findAll(@Nullable Integer from, @Nullable Integer size);

    Category findById(Long id);

    Category create(NewCategory category);

    Category update(Category category);

    void delete(Long id);
}
