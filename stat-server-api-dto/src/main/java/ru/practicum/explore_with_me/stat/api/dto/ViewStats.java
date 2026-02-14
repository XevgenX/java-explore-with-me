package ru.practicum.explore_with_me.stat.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Статистика по посещениям")
public class ViewStats {
    @Schema(description = "Название сервиса", example = "ewm-main-service")
    private String app;

    @Schema(description = "URI сервиса", example = "/events/1")
    private String uri;

    @Schema(description = "Количество просмотров", example = "6")
    private Long hits;

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Long getHits() {
        return hits;
    }

    public void setHits(Long hits) {
        this.hits = hits;
    }
}
