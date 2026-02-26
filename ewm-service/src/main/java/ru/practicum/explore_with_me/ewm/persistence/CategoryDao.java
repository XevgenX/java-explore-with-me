package ru.practicum.explore_with_me.ewm.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;
import ru.practicum.explore_with_me.ewm.domain.repo.CategoryRepo;
import ru.practicum.explore_with_me.ewm.persistence.mapper.CategoryPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.CategoryRepository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CategoryDao implements CategoryRepo {
    private final CategoryRepository repository;
    private final CategoryPersistenceMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<Category> findAll(@Nullable Integer from, @Nullable Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return mapper.toDomain(repository.findAll(page));
    }

    @Transactional(readOnly = true)
    @Override
    public Category findById(Long id) {
        return mapper.toDomain(repository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Категория не найдена")));
    }

    @Transactional
    @Override
    public Category create(NewCategory category) {
        return mapper.toDomain(repository.save(mapper.toNewEntity(category)));
    }

    @Transactional
    @Override
    public Category update(Category category) {
        return mapper.toDomain(repository.save(mapper.toEntity(category)));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
