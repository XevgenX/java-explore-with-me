package ru.practicum.explore_with_me.stat.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;
import ru.practicum.explore_with_me.stat.api.mapper.HitsStatToViewStatsMapper;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.service.HitService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера статистики посещений")
public class ViewStatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HitService hitService;

    @Autowired
    private HitsStatToViewStatsMapper mapper;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> uris;
    private Boolean unique;
    private List<HitsStat> hitsStats;
    private List<ViewStats> expectedViewStats;

    @BeforeEach
    void setUp() {
        start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        end = LocalDateTime.of(2024, 1, 2, 0, 0, 0);
        uris = List.of("/events/1", "/events/2");
        unique = false;

        HitsStat stat1 = new HitsStat("ewm-main-service", "/events/1", 5);
        HitsStat stat2 = new HitsStat("ewm-main-service", "/events/2", 3);
        hitsStats = List.of(stat1, stat2);

        expectedViewStats = mapper.convert(hitsStats);
    }

    @Test
    @DisplayName("Должен успешно получить статистику со всеми параметрами")
    void getStat_WithAllParameters_ShouldReturnStats() throws Exception {
        when(hitService.getByFilter(start, end, uris, unique))
                .thenReturn(hitsStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].app", is("ewm-main-service")))
                .andExpect(jsonPath("$[0].uri", is("/events/1")))
                .andExpect(jsonPath("$[0].hits", is(5)))
                .andExpect(jsonPath("$[1].app", is("ewm-main-service")))
                .andExpect(jsonPath("$[1].uri", is("/events/2")))
                .andExpect(jsonPath("$[1].hits", is(3)));

        verify(hitService, times(1)).getByFilter(start, end, uris, unique);
    }

    @Test
    @DisplayName("Должен успешно получить статистику без параметра unique")
    void getStat_WithoutUniqueParameter_ShouldReturnStats() throws Exception {
        when(hitService.getByFilter(start, end, uris, null))
                .thenReturn(hitsStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(hitService, times(1)).getByFilter(start, end, uris, null);
    }

    @Test
    @DisplayName("Должен успешно получить статистику без параметра uris")
    void getStat_WithoutUrisParameter_ShouldReturnStats() throws Exception {
        when(hitService.getByFilter(start, end, null, unique))
                .thenReturn(hitsStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(hitService, times(1)).getByFilter(start, end, null, unique);
    }

    @Test
    @DisplayName("Должен вернуть пустой список при отсутствии статистики")
    void getStat_WithNoStats_ShouldReturnEmptyList() throws Exception {
        when(hitService.getByFilter(start, end, uris, unique))
                .thenReturn(List.of());

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(hitService, times(1)).getByFilter(start, end, uris, unique);
    }

    @Test
    @DisplayName("Должен вернуть 400 когда start позже end")
    void getStat_WhenStartAfterEnd_ShouldReturnBadRequest() throws Exception {
        LocalDateTime laterStart = LocalDateTime.of(2024, 1, 3, 0, 0, 0);
        LocalDateTime earlierEnd = LocalDateTime.of(2024, 1, 2, 0, 0, 0);

        when(hitService.getByFilter(laterStart, earlierEnd, uris, unique))
                .thenThrow(new ValidationException("Дата начала не должна быть после даты окончания"));

        mockMvc.perform(get("/stats")
                        .param("start", laterStart.format(DATE_FORMATTER))
                        .param("end", earlierEnd.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(hitService, times(1)).getByFilter(laterStart, earlierEnd, uris, unique);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать параметр unique = true")
    void getStat_WithUniqueTrue_ShouldCallServiceWithUniqueTrue() throws Exception {
        Boolean uniqueParam = true;

        when(hitService.getByFilter(start, end, uris, uniqueParam))
                .thenReturn(hitsStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", uniqueParam.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));

        verify(hitService, times(1)).getByFilter(start, end, uris, uniqueParam);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать несколько URI")
    void getStat_WithMultipleUris_ShouldCallServiceWithAllUris() throws Exception {
        List<String> multipleUris = List.of("/events/1", "/events/2", "/events/3", "/events/4");

        HitsStat stat1 = new HitsStat("ewm-main-service", "/events/1", 5);
        HitsStat stat2 = new HitsStat("ewm-main-service", "/events/2", 3);
        HitsStat stat3 = new HitsStat("ewm-main-service", "/events/3", 7);
        HitsStat stat4 = new HitsStat("ewm-main-service", "/events/4", 2);
        List<HitsStat> multipleStats = List.of(stat1, stat2, stat3, stat4);

        when(hitService.getByFilter(start, end, multipleUris, unique))
                .thenReturn(multipleStats);

        mockMvc.perform(get("/stats")
                        .param("start", start.format(DATE_FORMATTER))
                        .param("end", end.format(DATE_FORMATTER))
                        .param("uris", multipleUris.toArray(new String[0]))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(4)))
                .andExpect(jsonPath("$[2].uri", is("/events/3")))
                .andExpect(jsonPath("$[2].hits", is(7)));

        verify(hitService, times(1)).getByFilter(start, end, multipleUris, unique);
    }

    @Test
    @DisplayName("Должен корректно обрабатывать граничные значения дат")
    void getStat_WithBoundaryDates_ShouldReturnStats() throws Exception {
        LocalDateTime boundaryStart = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime boundaryEnd = LocalDateTime.of(2024, 1, 1, 23, 59, 59);

        HitsStat stat = new HitsStat("ewm-main-service", "/events/1", 5);

        when(hitService.getByFilter(boundaryStart, boundaryEnd, uris, unique))
                .thenReturn(List.of(stat));

        mockMvc.perform(get("/stats")
                        .param("start", boundaryStart.format(DATE_FORMATTER))
                        .param("end", boundaryEnd.format(DATE_FORMATTER))
                        .param("uris", uris.toArray(new String[0]))
                        .param("unique", unique.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].hits", is(5)));

        verify(hitService, times(1)).getByFilter(boundaryStart, boundaryEnd, uris, unique);
    }
}
