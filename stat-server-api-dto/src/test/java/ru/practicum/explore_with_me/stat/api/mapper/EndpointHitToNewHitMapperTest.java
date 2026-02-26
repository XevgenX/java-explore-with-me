package ru.practicum.explore_with_me.stat.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EndpointHitToNewHitMapperTest {
    private EndpointHitToNewHitMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EndpointHitToNewHitMapper();
    }

    @Test
    void convert_whenSourceIsNull_shouldReturnNull() {
        NewHit result = mapper.convert(null);

        assertNull(result);
    }

    @Test
    void convert_whenSourceIsValid_shouldReturnNewHit() {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp("ewm-main-service");
        endpointHit.setUri("/events/1");
        endpointHit.setIp("192.163.0.1");
        LocalDateTime timestamp = LocalDateTime.now();
        endpointHit.setTimestamp(timestamp);

        NewHit result = mapper.convert(endpointHit);

        assertNotNull(result);
        assertEquals("ewm-main-service", result.getServer());
        assertEquals("/events/1", result.getUri());
        assertEquals("192.163.0.1", result.getIp());
        assertEquals(timestamp, result.getTime());
    }
}
