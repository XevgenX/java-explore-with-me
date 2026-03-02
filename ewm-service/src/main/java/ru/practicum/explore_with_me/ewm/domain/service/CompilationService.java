package ru.practicum.explore_with_me.ewm.domain.service;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.explore_with_me.common.domain.validator.ObjectValidatable;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.repo.CompilationRepo;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CompilationService implements ObjectValidatable {
    private final CompilationRepo repo;

    public List<Compilation> findAll(@Nullable Boolean pinned,
                              @Nullable Integer from,
                              @Nullable Integer size) {
        return repo.findAll(pinned, from, size);
    }

    public Compilation findById(Long id) {
        validate(id, "id не должен быть null");
        return repo.findById(id);
    }

    public Compilation create(NewCompilation compilation) {
        validate(compilation, "compilation не должен быть null");
        return repo.create(compilation);
    }

    public Compilation update(UpdatingCompilation compilation) {
        validate(compilation, "compilation не должен быть null");
        return repo.update(compilation);
    }

    public void delete(Long id) {
        validate(id, "id не должен быть null");
        repo.delete(id);
    }
}
