package ru.practicum.explore_with_me.ewm.persistence.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;
import ru.practicum.explore_with_me.ewm.persistence.entity.CategoryEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class CategoryPersistenceMapper {
    public Category toDomain(CategoryEntity entity) {
        if (entity == null) {
            return null;
        }
        return Category.builder()
                .id(entity.getId())
                .name(entity.getName())
                .build();
    }

    public List<Category> toDomain(List<CategoryEntity> entities) {
        return entities.stream().map(this::toDomain).collect(Collectors.toList());
    }

    public CategoryEntity toEntity(Category domain) {
        if (domain == null) {
            return null;
        }

        return CategoryEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    public void updateEntityFromDomain(Category domain, CategoryEntity entity) {
        if (domain == null || entity == null) {
            return;
        }
        if (Objects.nonNull(domain.getName()) && !domain.getName().isBlank()) {
            entity.setName(domain.getName());
        }
    }

    public CategoryEntity toNewEntity(NewCategory domain) {
        if (domain == null) {
            return null;
        }

        return CategoryEntity.builder()
                .name(domain.getName())
                .build();
    }
}
