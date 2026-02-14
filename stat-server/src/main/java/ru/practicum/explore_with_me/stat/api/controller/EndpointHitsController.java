package ru.practicum.explore_with_me.stat.api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.api.mapper.EndpointHitToNewHitMapper;
import ru.practicum.explore_with_me.stat.api.mapper.HitToEndpointHitMapper;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;
import ru.practicum.explore_with_me.stat.domain.service.HitService;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/hit")
@Tag(name = "StatsController", description = "API для работы со статистикой посещений")
public class EndpointHitsController {
    private final HitService service;
    private final HitToEndpointHitMapper hitToEndpointHitMapper;
    private final EndpointHitToNewHitMapper endpointHitToNewHitMapper;

    @Operation(summary = "Сохранение информации о том, что к эндпоинту был запрос",
            description = "Сохранение информации о том, что на uri конкретного сервиса был отправлен запрос пользователем. Название сервиса, uri и ip пользователя указаны в теле запроса.",
            operationId = "hit"
    )
    @PostMapping(consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<EndpointHit> save(@Parameter(description = "данные запроса", required = true) @RequestBody EndpointHit endpointHit) {
        NewHit newHit = endpointHitToNewHitMapper.convert(endpointHit);
        Hit savedHit = service.create(newHit);
        EndpointHit savedEndpointHit = hitToEndpointHitMapper.convert(savedHit);
        return ResponseEntity.created(URI.create("/hit")).body(savedEndpointHit);
    }
}
