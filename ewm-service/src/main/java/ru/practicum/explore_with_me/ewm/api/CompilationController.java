package ru.practicum.explore_with_me.ewm.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.CompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.NewCompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CompilationApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.service.CompilationService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CompilationController {
    private final CompilationService compilationService;
    private final EventService eventService;
    private final CompilationApiMapper mapper;

    @GetMapping("/compilations")
    public ResponseEntity<List<CompilationDto>> findAll(@RequestParam(value = "pinned", required = false) Boolean pined,
                                                        @RequestParam(value = "from", defaultValue = "0", required = false) Integer from,
                                                        @RequestParam(value = "size", defaultValue = "10", required = false) Integer size,
                                                        HttpServletRequest request) {
        List<CompilationDto> list = mapper.toDtoList(compilationService.findAll(pined, from, size));
        return ResponseEntity.ok(list);
    }

    @GetMapping("/compilations/{compId}")
    public ResponseEntity<CompilationDto> findById(@PathVariable Long compId,
                                                   HttpServletRequest request) {
        return ResponseEntity.ok(mapper.toDto(compilationService.findById(compId)));
    }

    @PostMapping("/admin/compilations")
    public ResponseEntity<CompilationDto> create(@RequestBody @Valid NewCompilationDto dto,
                                                 HttpServletRequest request) {
        List<Event> events = Collections.emptyList();
        if (Objects.nonNull(dto.getEvents())) {
            events = dto.getEvents().stream().map(eventService::findById).collect(Collectors.toList());
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toDto(compilationService.create(mapper.toNewModel(dto, events))));
    }

    @PatchMapping("/admin/compilations/{compId}")
    public ResponseEntity<CompilationDto> update(@PathVariable Long compId,
                                                 @RequestBody @Valid UpdateCompilationRequestDto dto,
                                                 HttpServletRequest request) {
        List<Event> events = Collections.emptyList();
        if (Objects.nonNull(dto.getEvents())) {
            events = dto.getEvents().stream().map(eventService::findById).collect(Collectors.toList());
        }
        Compilation compilation = compilationService.update(mapper.toUpdateModel(dto, compId, events));
        return ResponseEntity.ok(mapper.toDto(compilation));
    }

    @DeleteMapping("/admin/compilations/{compId}")
    public ResponseEntity<Void> delete(@PathVariable Long compId,
                                       HttpServletRequest request) {
        compilationService.delete(compId);
        return ResponseEntity.noContent().build();
    }
}
