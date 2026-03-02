package ru.practicum.explore_with_me.ewm.persistence.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.persistence.entity.CompilationEntity;
import ru.practicum.explore_with_me.ewm.persistence.entity.EventEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CompilationPersistenceMapper {
    private final EventPersistenceMapper eventMapper;

    public CompilationEntity toEntity(NewCompilation newCompilation) {
        if (newCompilation == null) {
            return null;
        }
        CompilationEntity entity = new CompilationEntity();
        entity.setTitle(newCompilation.getTitle());
        List<EventEntity> eventEntities = eventMapper.toEntities(newCompilation.getEvents());
        entity.setPined(newCompilation.getPined() != null ? newCompilation.getPined() : false);
        entity.setEvents(eventEntities != null ? eventEntities : Collections.emptyList());

        return entity;
    }

    // Преобразование Compilation в сущность (для обновления)
    public CompilationEntity toEntity(Compilation compilation, List<EventEntity> eventEntities) {
        if (compilation == null) {
            return null;
        }

        CompilationEntity entity = new CompilationEntity();
        entity.setId(compilation.getId());
        entity.setTitle(compilation.getTitle());
        entity.setPined(compilation.getPined());
        entity.setEvents(eventEntities != null ? eventEntities : Collections.emptyList());

        return entity;
    }

    // Преобразование сущности в модель Compilation
    public Compilation toDomain(CompilationEntity entity) {
        if (entity == null) {
            return null;
        }

        List<Event> events = entity.getEvents() != null ?
                entity.getEvents().stream()
                        .map(eventMapper::toDomain)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return new Compilation(
                entity.getId(),
                entity.getTitle(),
                entity.getPined(),
                events
        );
    }

    // Преобразование списка сущностей в список моделей
    public List<Compilation> toDomainList(List<CompilationEntity> entities) {
        if (entities == null) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    public CompilationEntity updateEntity(UpdatingCompilation compilation, CompilationEntity entity) {
        if (compilation == null || entity == null) {
            throw new ValidationException("Подборка невалидна");
        }

        if (compilation.getTitle() != null) {
            entity.setTitle(compilation.getTitle());
        }

        if (compilation.getPined() != null) {
            entity.setPined(compilation.getPined());
        }

        List<EventEntity> eventEntities = eventMapper.toEntities(compilation.getEvents());
        if (eventEntities != null) {
            entity.setEvents(eventEntities);
        }
        return entity;
    }
}
