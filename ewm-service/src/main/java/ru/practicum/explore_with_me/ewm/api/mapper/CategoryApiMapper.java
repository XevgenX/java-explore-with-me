package ru.practicum.explore_with_me.ewm.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.category.CategoryDto;
import ru.practicum.explore_with_me.ewm.api.dto.category.NewCategoryDto;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryApiMapper {
    public Category toModel(CategoryDto userDto) {
        if (userDto == null) {
            return null;
        }

        return Category.builder()
                .id(userDto.getId())
                .name(userDto.getName())
                .build();
    }

    public NewCategory toModel(NewCategoryDto userDto) {
        if (userDto == null) {
            return null;
        }

        return NewCategory.builder()
                .name(userDto.getName())
                .build();
    }

    public List<Category> toModels(List<CategoryDto> userDtos) {
        if (userDtos == null) {
            return null;
        }

        return userDtos.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public CategoryDto toDto(Category user) {
        if (user == null) {
            return null;
        }

        return CategoryDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public List<CategoryDto> toDtos(List<Category> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
