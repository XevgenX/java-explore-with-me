package ru.practicum.explore_with_me.stat.api.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;

import static org.junit.jupiter.api.Assertions.*;

public class HitsStatToViewStatsMapperTest {
    private HitsStatToViewStatsMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new HitsStatToViewStatsMapper();
    }

    @Test
    void convert_whenSourceIsNull_shouldReturnNull() {
        HitsStat stat = null;
        ViewStats result = mapper.convert(stat);

        assertNull(result);
    }

    @Test
    void convert_whenSourceIsValid_shouldReturnViewStats() {
        HitsStat hitsStat = new HitsStat("ewm-main-service", "/events/1", 6);

        ViewStats result = mapper.convert(hitsStat);

        assertNotNull(result);
        assertEquals("ewm-main-service", result.getApp());
        assertEquals("/events/1", result.getUri());
        assertEquals(6L, result.getHits());
    }

    @Test
    void convert_whenHitsIsZero_shouldReturnViewStatsWithZeroHits() {
        HitsStat hitsStat = new HitsStat("service", "/uri", 0);

        ViewStats result = mapper.convert(hitsStat);

        assertNotNull(result);
        assertEquals(0L, result.getHits());
    }

    @Test
    void convert_whenHitsIsNegative_shouldReturnViewStatsWithNegativeHits() {
        HitsStat hitsStat = new HitsStat("service", "/uri", -5);

        ViewStats result = mapper.convert(hitsStat);

        assertNotNull(result);
        assertEquals(-5L, result.getHits());
    }
}
