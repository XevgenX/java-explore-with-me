package ru.practicum.explore_with_me.ewm.domain.repo;

import jakarta.annotation.Nullable;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;

import java.util.List;

public interface CompilationRepo {
    List<Compilation> findAll(@Nullable Boolean pinned,
                              @Nullable Integer from,
                              @Nullable Integer size);

    Compilation findById(Long id);

    Compilation create(NewCompilation compilation);

    Compilation update(UpdatingCompilation compilation);

    void delete(Long id);
}
