package ru.practicum.explore_with_me.stat.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Класс NewHit")
public class NewHitTest {
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("Новый сервис должен успешно создаваться с корректными параметрами")
    void shouldCreateNewHitWithValidParameters() {
        String validUri = "/api/test";
        String validIp = "192.168.1.1";

        NewHit newHit = new NewHit("server", validUri, validIp, now);

        assertNotNull(newHit);
        assertEquals(validUri, newHit.getUri());
        assertEquals(validIp, newHit.getIp());
        assertEquals(now, newHit.getTime());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null URI")
    void shouldThrowExceptionWhenNullUri() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server",null, "192.168.1.1", now)
        );
        assertEquals("У запроса должен быть корректным URI", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при пустом URI")
    void shouldThrowExceptionWhenEmptyUri() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","", "192.168.1.1", now)
        );
        assertEquals("У запроса должен быть корректным URI", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при URI из пробелов")
    void shouldThrowExceptionWhenBlankUri() {
        ValidationException exception1 = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","   ", "192.168.1.1", now)
        );
        assertEquals("У запроса должен быть корректным URI", exception1.getMessage());

        ValidationException exception2 = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server"," ", "192.168.1.1", now)
        );
        assertEquals("У запроса должен быть корректным URI", exception2.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null IP")
    void shouldThrowExceptionWhenNullIp() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", null, now)
        );
        assertEquals("У запроса должен корректным быть IP", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при пустом IP")
    void shouldThrowExceptionWhenEmptyIp() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", "", now)
        );
        assertEquals("У запроса должен корректным быть IP", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при IP из пробелов")
    void shouldThrowExceptionWhenBlankIp() {
        ValidationException exception1 = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", "   ", now)
        );
        assertEquals("У запроса должен корректным быть IP", exception1.getMessage());

        ValidationException exception2 = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", " ", now)
        );
        assertEquals("У запроса должен корректным быть IP", exception2.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null времени")
    void shouldThrowExceptionWhenNullTime() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", "192.168.1.1", null)
        );
        assertEquals("У запроса должно быть корректное время", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при слишком свежем времени")
    void shouldThrowExceptionWhenTimeIsTooRecent() {
        LocalDateTime recentTime = LocalDateTime.now().minusMinutes(6);

        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new NewHit("server","/api/test", "192.168.1.1", recentTime)
        );
        assertEquals("У запроса должно быть корректное время", exception.getMessage());
    }

    @Test
    @DisplayName("Должен использовать Builder для создания объекта")
    void shouldUseBuilderPattern() {
        NewHit newHit = NewHit.builder()
                .server("server")
                .uri("/api/test")
                .ip("192.168.1.1")
                .time(now)
                .build();

        assertNotNull(newHit);
        assertEquals("/api/test", newHit.getUri());
        assertEquals("192.168.1.1", newHit.getIp());
        assertEquals(now, newHit.getTime());
    }
}
