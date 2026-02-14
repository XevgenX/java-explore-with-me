package ru.practicum.explore_with_me.stat.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.domain.model.Hit;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class HitToEndpointHitMapperTest {
    private HitToEndpointHitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new HitToEndpointHitMapper();
    }

    @Test
    void convert_whenSourceIsNull_shouldReturnNull() {
        EndpointHit result = mapper.convert(null);

        assertNull(result);
    }

    @Test
    void convert_whenSourceIsValid_shouldReturnEndpointHit() {
        LocalDateTime time = LocalDateTime.now();
        Hit hit = Hit.builder()
                .id(1L)
                .server("ewm-main-service")
                .uri("/events/1")
                .ip("192.163.0.1")
                .time(time)
                .build();

        EndpointHit result = mapper.convert(hit);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("ewm-main-service", result.getApp());
        assertEquals("/events/1", result.getUri());
        assertEquals("192.163.0.1", result.getIp());
        assertEquals(time, result.getTimestamp());
    }
}
