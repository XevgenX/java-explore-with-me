package ru.practicum.explore_with_me.ewm.persistence.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.ewm.persistence.entity.CategoryEntity;

import java.util.List;

@Repository
public interface CategoryRepository extends CrudRepository<CategoryEntity, Long> {
    List<CategoryEntity> findAll(Pageable page);
}
