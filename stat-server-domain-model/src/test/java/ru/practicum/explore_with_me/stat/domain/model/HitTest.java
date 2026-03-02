package ru.practicum.explore_with_me.stat.domain.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.practicum.explore_with_me.common.domain.exception.ValidationException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Класс Hit")
class HitTest {

    private LocalDateTime oldDateTime;

    @BeforeEach
    void setUp() {
        oldDateTime = LocalDateTime.now().minusMinutes(4);
    }

    @Test
    @DisplayName("Должен успешно создаваться с корректными параметрами")
    void shouldCreateHitWithValidParameters() {
        Hit hit = new Hit(1L, "server", "/api/test", "192.168.1.1", oldDateTime);

        assertNotNull(hit);
        assertEquals(1L, hit.getId());
        assertEquals("/api/test", hit.getUri());
        assertEquals("192.168.1.1", hit.getIp());
        assertEquals(oldDateTime, hit.getTime());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null id")
    void shouldThrowExceptionWhenNullId() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(null, "server", "/api/test", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть id", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при отрицательном id")
    void shouldThrowExceptionWhenNegativeId() {
        ValidationException exception1 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(-1L, "server", "/api/test", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть id", exception1.getMessage());

        ValidationException exception2 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(-100L, "server", "/api/test", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть id", exception2.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null URI")
    void shouldThrowExceptionWhenNullUri() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", null, "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен быть корректным URI", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при пустом URI")
    void shouldThrowExceptionWhenEmptyUri() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен быть корректным URI", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при URI из пробелов")
    void shouldThrowExceptionWhenBlankUri() {
        ValidationException exception1 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "   ", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен быть корректным URI", exception1.getMessage());

        ValidationException exception2 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", " ", "192.168.1.1", oldDateTime)
        );
        assertEquals("У запроса должен быть корректным URI", exception2.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при null IP")
    void shouldThrowExceptionWhenNullIp() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "/api/test", null, oldDateTime)
        );
        assertEquals("У запроса должен корректным быть IP", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при пустом IP")
    void shouldThrowExceptionWhenEmptyIp() {
        ValidationException exception = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "/api/test", "", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть IP", exception.getMessage());
    }

    @Test
    @DisplayName("Должен выбрасывать ValidationException при IP из пробелов")
    void shouldThrowExceptionWhenBlankIp() {
        ValidationException exception1 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "/api/test", "   ", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть IP", exception1.getMessage());

        ValidationException exception2 = assertThrowsExactly(
                ValidationException.class,
                () -> new Hit(1L, "server", "/api/test", " ", oldDateTime)
        );
        assertEquals("У запроса должен корректным быть IP", exception2.getMessage());
    }
}
