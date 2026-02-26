package ru.practicum.explore_with_me.stat.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatClientTest {

    @Mock
    private RestTemplate restTemplate;

    private StatClient statClient;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SERVER_URL = "http://localhost:9090";
    private static final String HIT_ENDPOINT = "/hit";

    @Captor
    private ArgumentCaptor<String> urlCaptor;

    @Captor
    private ArgumentCaptor<HttpEntity<EndpointHit>> requestEntityCaptor;

    @BeforeEach
    void setUp() {
        statClient = new StatClient(restTemplate);
        ReflectionTestUtils.setField(statClient, "serverUrl", SERVER_URL);
    }

    @Test
    void saveHit_ShouldReturnEndpointHit_WhenResponseIs201Created() {
        EndpointHit hit = createTestHit();
        EndpointHit expectedResponse = createTestHit();
        expectedResponse.setId(1L);

        HttpHeaders expectedHeaders = new HttpHeaders();
        expectedHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<EndpointHit> expectedRequestEntity = new HttpEntity<>(hit, expectedHeaders);

        ResponseEntity<EndpointHit> responseEntity = new ResponseEntity<>(expectedResponse, HttpStatus.CREATED);
        String expectedUrl = SERVER_URL + HIT_ENDPOINT;

        when(restTemplate.postForEntity(
                eq(expectedUrl),
                any(HttpEntity.class),
                eq(EndpointHit.class)
        )).thenReturn(responseEntity);

        EndpointHit result = statClient.saveHit(hit);

        assertNotNull(result);
        assertEquals(expectedResponse.getId(), result.getId());
        assertEquals(expectedResponse.getApp(), result.getApp());
        assertEquals(expectedResponse.getUri(), result.getUri());
        assertEquals(expectedResponse.getIp(), result.getIp());
        assertEquals(expectedResponse.getTimestamp(), result.getTimestamp());

        verify(restTemplate).postForEntity(
                eq(expectedUrl),
                requestEntityCaptor.capture(),
                eq(EndpointHit.class)
        );

        HttpEntity<EndpointHit> capturedRequest = requestEntityCaptor.getValue();
        assertEquals(MediaType.APPLICATION_JSON, capturedRequest.getHeaders().getContentType());
        assertEquals(hit.getApp(), capturedRequest.getBody().getApp());
        assertEquals(hit.getUri(), capturedRequest.getBody().getUri());
    }

    @Test
    void saveHit_ShouldThrowException_WhenRestTemplateThrowsException() {
        EndpointHit hit = createTestHit();
        String expectedUrl = SERVER_URL + HIT_ENDPOINT;

        when(restTemplate.postForEntity(
                eq(expectedUrl),
                any(HttpEntity.class),
                eq(EndpointHit.class)
        )).thenThrow(new RuntimeException("Connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statClient.saveHit(hit));
        assertTrue(exception.getMessage().contains("Error saving hit"));
    }

    @Test
    void getStats_ShouldWorkWithEmptyUrisList() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        List<String> uris = List.of();

        List<ViewStats> expectedStats = List.of(createViewStats("app1", "/events", 15L));

        ResponseEntity<List<ViewStats>> responseEntity = new ResponseEntity<>(expectedStats, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<ViewStats> result = statClient.getStats(start, end, uris, null);

        assertNotNull(result);
        assertEquals(1, result.size());

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );

        String capturedUrl = urlCaptor.getValue();
        assertFalse(capturedUrl.contains("uris="));
    }

    @Test
    void getStats_ShouldThrowException_WhenRestTemplateThrowsException() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 31, 23, 59, 59);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenThrow(new RuntimeException("Connection error"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> statClient.getStats(start, end, null, null));
        assertTrue(exception.getMessage().contains("Error getting stats"));
    }

    @Test
    void getStats_ShouldHandleMultipleUrisCorrectly() {
        LocalDateTime start = LocalDateTime.of(2023, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2023, 12, 31, 23, 59, 59);
        List<String> uris = List.of("/events/1", "/events/2", "/events/3");

        List<ViewStats> expectedStats = List.of(createViewStats("app1", "/events/1", 10L));

        ResponseEntity<List<ViewStats>> responseEntity = new ResponseEntity<>(expectedStats, HttpStatus.OK);

        when(restTemplate.exchange(
                anyString(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        List<ViewStats> result = statClient.getStats(start, end, uris, false);

        assertNotNull(result);

        verify(restTemplate).exchange(
                urlCaptor.capture(),
                eq(HttpMethod.GET),
                isNull(),
                any(ParameterizedTypeReference.class)
        );

        String capturedUrl = urlCaptor.getValue();
        assertTrue(capturedUrl.contains("uris=/events/1"));
        assertTrue(capturedUrl.contains("uris=/events/2"));
        assertTrue(capturedUrl.contains("uris=/events/3"));
        assertTrue(capturedUrl.contains("unique=false"));
    }

    private EndpointHit createTestHit() {
        EndpointHit hit = new EndpointHit();
        hit.setApp("test-app");
        hit.setUri("/test/uri");
        hit.setIp("192.168.1.1");
        hit.setTimestamp(LocalDateTime.now());
        return hit;
    }

    private ViewStats createViewStats(String app, String uri, Long hits) {
        ViewStats viewStats = new ViewStats();
        viewStats.setApp(app);
        viewStats.setUri(uri);
        viewStats.setHits(hits);
        return viewStats;
    }
}
