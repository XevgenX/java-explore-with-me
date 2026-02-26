package ru.practicum.explore_with_me.stat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;
import ru.practicum.explore_with_me.stat.api.mapper.HitsStatToViewStatsMapper;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.service.HitService;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/stats")
@Tag(name = "StatsController", description = "API для работы со статистикой посещений")
public class ViewStatsController {
    private final HitService service;
    private final HitsStatToViewStatsMapper mapper;

    @GetMapping
    @Operation(summary = "Получение статистики по посещениям. Обратите внимание: значение даты и времени нужно закодировать (например используя java.net.URLEncoder.encode) ",
            operationId = "getStats"
    )
    public ResponseEntity<List<ViewStats>> getStat(@Parameter(description = "Дата и время начала диапазона за который нужно выгрузить статистику (в формате \"yyyy-MM-dd HH:mm:ss\")",
                                                               required = true)
                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                       @RequestParam LocalDateTime start,
                                                   @Parameter(description = "Дата и время конца диапазона за который нужно выгрузить статистику (в формате \"yyyy-MM-dd HH:mm:ss\")",
                                                           required = true)
                                                   @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                   @RequestParam LocalDateTime end,
                                                   @Parameter(description = "Список uri для которых нужно выгрузить статистику",
                                                           required = false)
                                                       @RequestParam(required = false) List<String> uris,
                                                   @Parameter(description = "Нужно ли учитывать только уникальные посещения (только с уникальным ip)",
                                                           required = false)
                                                       @RequestParam(required = false) Boolean unique) {
        List<HitsStat> stats = service.getByFilter(start, end, uris, unique);
        List<ViewStats> statsDto = mapper.convert(stats);
        return ResponseEntity.ok(statsDto);
    }
}
