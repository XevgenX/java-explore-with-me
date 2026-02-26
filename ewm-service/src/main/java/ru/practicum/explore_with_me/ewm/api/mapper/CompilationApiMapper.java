package ru.practicum.explore_with_me.ewm.api.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.CompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.NewCompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.explore_with_me.ewm.api.dto.event.EventShortDto;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CompilationApiMapper {
    private final EventApiMapper eventMapper;

    public NewCompilation toNewModel(NewCompilationDto dto, List<Event> events) {
        if (dto == null) {
            return null;
        }

        return new NewCompilation(
                dto.getTitle(),
                dto.getPinned() != null ? dto.getPinned() : false,
                events != null ? events : Collections.emptyList()
        );
    }

    public UpdatingCompilation toUpdateModel(UpdateCompilationRequestDto dto, Long id, List<Event> events) {
        if (dto == null) {
            return null;
        }

        return UpdatingCompilation.builder()
                .id(id)
                .title(dto.getTitle())
                .pined(dto.getPinned())
                .events(events != null ? events : Collections.emptyList())
                .build();
    }

    public CompilationDto toDto(Compilation compilation) {
        if (compilation == null) {
            return null;
        }

        List<EventShortDto> eventDtos = compilation.getEvents() != null ?
                compilation.getEvents().stream()
                        .map(eventMapper::toShortDto)
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        return CompilationDto.builder()
                .id(compilation.getId())
                .title(compilation.getTitle())
                .pinned(compilation.getPined())
                .events(eventDtos)
                .build();
    }

    public List<CompilationDto> toDtoList(List<Compilation> compilations) {
        if (compilations == null) {
            return Collections.emptyList();
        }

        return compilations.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
