package ru.practicum.explore_with_me.stat.persistence.dao;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@JdbcTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import(HitsDao.class)
@Sql(scripts = {"/schema.sql", "/testdata-h2.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class HitsDaoTest {
    @Autowired
    private HitsDao hitsDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void findAll_shouldReturnAllHits() {
        List<Hit> result = hitsDao.findAll();

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);

        Hit firstHit = result.get(0);
        assertThat(firstHit.getId()).isEqualTo(1L);
        assertThat(firstHit.getServer()).isEqualTo("server1.com");
        assertThat(firstHit.getUri()).isEqualTo("/api/users");
        assertThat(firstHit.getIp()).isEqualTo("192.168.1.1");
        assertThat(firstHit.getTime()).isEqualTo(LocalDateTime.of(2024, 1, 10, 10, 0));
    }

    @Test
    void create_shouldInsertNewHitAndReturnWithId() {
        LocalDateTime now = LocalDateTime.now();
        NewHit newHit = NewHit.builder()
                .server("server3.com")
                .uri("/api/orders")
                .ip("192.168.3.1")
                .time(now)
                .build();

        Hit created = hitsDao.create(newHit);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull().isPositive();
        assertThat(created.getServer()).isEqualTo("server3.com");
        assertThat(created.getUri()).isEqualTo("/api/orders");
        assertThat(created.getIp()).isEqualTo("192.168.3.1");
        assertThat(created.getTime()).isEqualTo(now);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM hits WHERE server = ? AND uri = ?",
                Integer.class,
                "server3.com", "/api/orders"
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByFilter_withoutUrisAndUniq_shouldReturnAllStatsInRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 12, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, null, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1); // Только server1.com:/api/users в этом диапазоне

        HitsStat stat = result.get(0);
        assertThat(stat.getServer()).isEqualTo("server1.com");
        assertThat(stat.getUri()).isEqualTo("/api/users");
        assertThat(stat.getHits()).isEqualTo(2); // Два хита с разными IP
    }

    @Test
    void findByFilter_withUniqFalse_shouldReturnAllHits() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 12, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, null, false);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(2);
    }

    @Test
    void findByFilter_withUniqTrue_shouldReturnUniqueIpCount() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 12, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, null, true);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(2); // Два уникальных IP
    }

    @Test
    void findByFilter_withUrisFilter_shouldReturnOnlyMatchingUris() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 0, 0);
        List<String> uris = Arrays.asList("/api/users", "/api/nonexistent");

        List<HitsStat> result = hitsDao.findByFilter(start, end, uris, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUri()).isEqualTo("/api/users");
        assertThat(result.get(0).getHits()).isEqualTo(2);
    }

    @Test
    void findByFilter_withEmptyUrisList_shouldIgnoreUrisFilter() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 9, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 15, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, Collections.emptyList(), null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(2); // Оба сервера в диапазоне
    }

    @Test
    void findByFilter_withSpecificTimeRange_shouldReturnOnlyHitsInRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 11, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 12, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, null, null);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getHits()).isEqualTo(1); // Только один хит в 11:00
    }

    @Test
    void findByFilter_withNoResults_shouldReturnEmptyList() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 2, 0, 0);

        List<HitsStat> result = hitsDao.findByFilter(start, end, null, null);

        assertThat(result).isNotNull().isEmpty();
    }

    @Test
    void mapHit_shouldCorrectlyMapResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(100L);
        when(rs.getString("server")).thenReturn("test-server.com");
        when(rs.getString("uri")).thenReturn("/test");
        when(rs.getString("ip")).thenReturn("10.0.0.1");
        when(rs.getTimestamp("time")).thenReturn(Timestamp.valueOf("2024-01-20 20:00:00"));

        Hit result = hitsDao.mapHit(rs, 0);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getServer()).isEqualTo("test-server.com");
        assertThat(result.getUri()).isEqualTo("/test");
        assertThat(result.getIp()).isEqualTo("10.0.0.1");
        assertThat(result.getTime()).isEqualTo(LocalDateTime.of(2024, 1, 20, 20, 0));
    }

    @Test
    void mapStat_shouldCorrectlyMapResultSet() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("server")).thenReturn("test-server.com");
        when(rs.getString("uri")).thenReturn("/test");
        when(rs.getInt("hits")).thenReturn(42);

        HitsStat result = hitsDao.mapStat(rs, 0);

        assertThat(result.getServer()).isEqualTo("test-server.com");
        assertThat(result.getUri()).isEqualTo("/test");
        assertThat(result.getHits()).isEqualTo(42);
    }

    @Test
    void constructor_shouldInitializeJdbcTemplates() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate();

        HitsDao dao = new HitsDao(jdbcTemplate);

        assertThat(dao).isNotNull();
    }
}
