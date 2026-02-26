package ru.practicum.explore_with_me.stat.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;
import ru.practicum.explore_with_me.stat.domain.repo.HitRepo;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HitServiceTest {

    @Mock
    private HitRepo hitRepo;

    @InjectMocks
    private HitService hitService;

    private NewHit validNewHit;
    private Hit createdHit;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().minusMinutes(10);
        endTime = LocalDateTime.now().plusMinutes(10);

        validNewHit = NewHit.builder()
                .server("test-server.com")
                .uri("/api/test")
                .ip("192.168.1.1")
                .time(LocalDateTime.now())
                .build();

        createdHit = Hit.builder()
                .id(1L)
                .server("test-server.com")
                .uri("/api/test")
                .ip("192.168.1.1")
                .time(LocalDateTime.now())
                .build();
    }

    @Test
    void create_withValidHit_shouldCallRepositoryAndReturnResult() {
        when(hitRepo.create(validNewHit)).thenReturn(createdHit);

        Hit result = hitService.create(validNewHit);

        assertThat(result).isEqualTo(createdHit);
        verify(hitRepo).create(validNewHit);
    }

    @Test
    void create_withNullHit_shouldThrowValidationException() {
        assertThatThrownBy(() -> hitService.create(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Данные о посещении не корректны");
    }

    @Test
    void create_whenRepositoryThrowsException_shouldPropagateIt() {
        when(hitRepo.create(validNewHit)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> hitService.create(validNewHit))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    void getAll_shouldReturnAllHitsFromRepository() {
        List<Hit> expectedHits = Arrays.asList(
                createdHit,
                Hit.builder()
                        .id(2L)
                        .server("server2.com")
                        .uri("/api/products")
                        .ip("192.168.1.2")
                        .time(LocalDateTime.of(2024, 1, 16, 13, 0))
                        .build()
        );
        when(hitRepo.findAll()).thenReturn(expectedHits);

        List<Hit> result = hitService.getAll();

        assertThat(result).isEqualTo(expectedHits);
        assertThat(result).hasSize(2);
        verify(hitRepo).findAll();
    }

    @Test
    void getAll_whenNoHits_shouldReturnEmptyList() {
        when(hitRepo.findAll()).thenReturn(Collections.emptyList());

        List<Hit> result = hitService.getAll();

        assertThat(result).isEmpty();
        verify(hitRepo).findAll();
    }

    @Test
    void getByFilter_withValidParameters_shouldCallRepositoryAndReturnResult() {
        List<String> uris = Arrays.asList("/api/users", "/api/products");
        Boolean uniq = true;

        List<HitsStat> expectedStats = Arrays.asList(
                HitsStat.builder()
                        .server("server1.com")
                        .uri("/api/users")
                        .hits(5)
                        .build(),
                HitsStat.builder()
                        .server("server2.com")
                        .uri("/api/products")
                        .hits(3)
                        .build()
        );

        when(hitRepo.findByFilter(startTime, endTime, uris, uniq))
                .thenReturn(expectedStats);

        List<HitsStat> result = hitService.getByFilter(startTime, endTime, uris, uniq);

        assertThat(result).isEqualTo(expectedStats);
        assertThat(result).hasSize(2);
        verify(hitRepo).findByFilter(startTime, endTime, uris, uniq);
    }

    @Test
    void getByFilter_withNullUrisAndUniq_shouldCallRepositoryWithNulls() {
        List<HitsStat> expectedStats = Collections.singletonList(
                HitsStat.builder()
                        .server("server1.com")
                        .uri("/api/users")
                        .hits(10)
                        .build()
        );

        when(hitRepo.findByFilter(startTime, endTime, null, null))
                .thenReturn(expectedStats);

        List<HitsStat> result = hitService.getByFilter(startTime, endTime, null, null);

        assertThat(result).isEqualTo(expectedStats);
        verify(hitRepo).findByFilter(startTime, endTime, null, null);
    }

    @Test
    void getByFilter_withEmptyUrisList_shouldPassEmptyListToRepository() {
        List<String> emptyUris = Collections.emptyList();
        List<HitsStat> expectedStats = Arrays.asList(
                HitsStat.builder()
                        .server("server1.com")
                        .uri("/api/users")
                        .hits(2)
                        .build(),
                HitsStat.builder()
                        .server("server2.com")
                        .uri("/api/products")
                        .hits(1)
                        .build()
        );

        when(hitRepo.findByFilter(startTime, endTime, emptyUris, false))
                .thenReturn(expectedStats);

        List<HitsStat> result = hitService.getByFilter(startTime, endTime, emptyUris, false);

        assertThat(result).isEqualTo(expectedStats);
        verify(hitRepo).findByFilter(startTime, endTime, emptyUris, false);
    }

    @Test
    void getByFilter_withNullStartDate_shouldThrowValidationException() {
        assertThatThrownBy(() -> hitService.getByFilter(null, endTime, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата начала фильтрации не корректна");
    }

    @Test
    void getByFilter_withNullEndDate_shouldThrowValidationException() {
        assertThatThrownBy(() -> hitService.getByFilter(startTime, null, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата окончания фильтрации не корректна");
    }

    @Test
    void getByFilter_withStartAfterEnd_shouldThrowValidationException() {
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 1, 0, 0);

        assertThatThrownBy(() -> hitService.getByFilter(start, end, null, null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Дата начала не должна быть после даты окончания");
    }

    @Test
    void getByFilter_withSameStartAndEnd_shouldCallRepository() {
        LocalDateTime sameTime = LocalDateTime.of(2024, 1, 15, 12, 0);
        List<HitsStat> expectedStats = Collections.singletonList(
                HitsStat.builder()
                        .server("server1.com")
                        .uri("/api/users")
                        .hits(1)
                        .build()
        );

        when(hitRepo.findByFilter(sameTime, sameTime, null, null))
                .thenReturn(expectedStats);

        List<HitsStat> result = hitService.getByFilter(sameTime, sameTime, null, null);

        assertThat(result).isEqualTo(expectedStats);
        verify(hitRepo).findByFilter(sameTime, sameTime, null, null);
    }

    @Test
    void getByFilter_whenRepositoryReturnsEmptyList_shouldReturnEmptyList() {
        when(hitRepo.findByFilter(startTime, endTime, null, null))
                .thenReturn(Collections.emptyList());

        List<HitsStat> result = hitService.getByFilter(startTime, endTime, null, null);

        assertThat(result).isEmpty();
        verify(hitRepo).findByFilter(startTime, endTime, null, null);
    }

    @Test
    void getByFilter_withInvalidHitObject_shouldThrowExceptionBeforeCallingRepo() {
        NewHit invalidHit = null;

        assertThatThrownBy(() -> hitService.create(invalidHit))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Данные о посещении не корректны");
    }

    @Test
    void getByFilter_withFutureStartDate_shouldBeValidIfEndDateIsFurther() {
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1);
        LocalDateTime futureEnd = LocalDateTime.now().plusDays(2);

        List<HitsStat> expectedStats = Collections.emptyList();
        when(hitRepo.findByFilter(futureStart, futureEnd, null, null))
                .thenReturn(expectedStats);

        List<HitsStat> result = hitService.getByFilter(futureStart, futureEnd, null, null);

        assertThat(result).isEmpty();
        verify(hitRepo).findByFilter(futureStart, futureEnd, null, null);
    }

    @Test
    void objectValidation_shouldWorkForDifferentInvalidInputs() {
        assertThatThrownBy(() -> hitService.create(null))
                .isInstanceOf(ValidationException.class);
    }
}
