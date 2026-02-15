package ru.practicum.explore_with_me.stat.persistence.dao;

import jakarta.annotation.Nullable;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.explore_with_me.stat.domain.model.Hit;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;
import ru.practicum.explore_with_me.stat.domain.model.NewHit;
import ru.practicum.explore_with_me.stat.domain.repo.HitRepo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public class HitsDao implements HitRepo {
    private static final String FIND_ALL_QUERY = """
            SELECT id, server, uri, ip, time
            FROM hits
            """;
    private static final String INSERT_QUERY = """
            INSERT INTO hits (server, uri, ip, time)
            VALUES (?, ?, ?, ?)
            """;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbcTemplate;

    public HitsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        namedJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
    }

    @Override
    public Hit create(NewHit hit) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    INSERT_QUERY,
                    new String[]{"id"}
            );
            ps.setString(1, hit.getServer());
            ps.setString(2, hit.getUri());
            ps.setString(3, hit.getIp());
            ps.setTimestamp(4, Timestamp.valueOf(hit.getTime()));
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new DataAccessException("Ошибка при получении ID") {};
        }

        return new Hit(key.longValue(), hit);
    }

    @Override
    public List<Hit> findAll() {
        return jdbcTemplate.query(FIND_ALL_QUERY, this::mapHit);
    }

    @Override
    public List<HitsStat> findByFilter(LocalDateTime start, LocalDateTime end,
                                       @Nullable List<String> uris,
                                       @Nullable Boolean uniq) {
        String countExpression = (uniq != null && uniq) ? "COUNT(DISTINCT ip)" : "COUNT(*)";

        // Строим запрос динамически
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT server, uri, ")
                .append(countExpression)
                .append(" as hits FROM hits WHERE time BETWEEN :start AND :end");

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("start", Timestamp.valueOf(start));
        params.addValue("end", Timestamp.valueOf(end));

        if (uris != null && !uris.isEmpty()) {
            queryBuilder.append(" AND uri IN (:uris)");
            params.addValue("uris", uris);
        }

        queryBuilder.append(" GROUP BY server, uri");
		queryBuilder.append(" ORDER BY hits DESC");

        return namedJdbcTemplate.query(
                queryBuilder.toString(),
                params,
                this::mapStat
        );
    }

    Hit mapHit(ResultSet rs, int rowNum) throws SQLException {
        return Hit.builder()
                .id(rs.getLong("id"))
                .server(rs.getString("server"))
                .uri(rs.getString("uri"))
                .ip(rs.getString("ip"))
                .time(rs.getTimestamp("time").toLocalDateTime())
                .build();
    }

    HitsStat mapStat(ResultSet rs, int rowNum) throws SQLException {
        return HitsStat.builder()
                .server(rs.getString("server"))
                .uri(rs.getString("uri"))
                .hits(rs.getInt("hits"))
                .build();
    }
}
