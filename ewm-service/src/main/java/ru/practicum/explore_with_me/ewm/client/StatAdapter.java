package ru.practicum.explore_with_me.ewm.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.ewm.domain.repo.StatRepo;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;
import ru.practicum.explore_with_me.stat.client.StatClient;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class StatAdapter implements StatRepo {
    private static final String EVENT_URL_TEMPLATE = "/events/%s";
    private final StatClient client;

    @Override
    public Long getViewsForEvent(Long eventId, LocalDateTime created) {
        Long views = 0L;
        try {
            List<ViewStats> stats = client.getStats(created,
                    LocalDateTime.now(),
                    List.of(String.format(EVENT_URL_TEMPLATE, eventId)), true);
            if (!stats.isEmpty()) {
                views = stats.get(0).getHits();;
            }
        } catch (Exception e) {
            log.error("Ошибка при запросе числа просмотров", e);
        }
        return views;
    }
}
