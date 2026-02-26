package ru.practicum.explore_with_me.stat.api.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.stat.api.dto.ViewStats;
import ru.practicum.explore_with_me.stat.domain.model.HitsStat;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class HitsStatToViewStatsMapper {
    public ViewStats convert(HitsStat source) {
        if (source == null) {
            return null;
        }

        ViewStats target = new ViewStats();
        target.setApp(source.getServer());
        target.setUri(source.getUri());
        target.setHits(source.getHits().longValue());

        return target;
    }

    public List<ViewStats> convert(List<HitsStat> source) {
        return source.stream().map(this::convert).collect(Collectors.toList());
    }
}
