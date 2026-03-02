package ru.practicum.explore_with_me.ewm.persistence;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.repo.CompilationRepo;
import ru.practicum.explore_with_me.ewm.persistence.entity.CompilationEntity;
import ru.practicum.explore_with_me.ewm.persistence.mapper.CompilationPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.repository.CompilationRepository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class CompilationDao implements CompilationRepo {
    private final CompilationRepository repository;
    private final CompilationPersistenceMapper mapper;

    @Transactional(readOnly = true)
    @Override
    public List<Compilation> findAll(@Nullable Boolean pinned, Integer from, Integer size) {
        Pageable page = PageRequest.of(from / size, size);
        return mapper.toDomainList(repository.findAll(pinned, page));
    }

    @Transactional(readOnly = true)
    @Override
    public Compilation findById(Long id) {
        return mapper.toDomain(repository
                .findByIdWithEvents(id).orElseThrow(() -> new NotFoundException("Подборка не найдена")));
    }

    @Transactional
    @Override
    public Compilation create(NewCompilation compilation) {
        return mapper.toDomain(repository.save(mapper.toEntity(compilation)));
    }

    @Transactional
    @Override
    public Compilation update(UpdatingCompilation compilation) {
        CompilationEntity savedEntity = repository
                .findById(compilation.getId()).orElseThrow(() -> new NotFoundException("Подборка не найдена"));
        return mapper.toDomain(repository.save(mapper.updateEntity(compilation, savedEntity)));
    }

    @Transactional
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
