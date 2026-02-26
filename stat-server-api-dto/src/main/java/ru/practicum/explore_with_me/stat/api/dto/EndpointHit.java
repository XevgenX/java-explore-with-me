package ru.practicum.explore_with_me.stat.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Информация о запросе к endpoint-у")
public class EndpointHit {
    @Schema(description = "Идентификатор записи", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Идентификатор сервиса для которого записывается информация",
            example = "ewm-main-service")
    private String app;

    @Schema(description = "URI для которого был осуществлен запрос",
            example = "/events/1")
    private String uri;

    @Schema(description = "IP-адрес пользователя, осуществившего запрос",
            example = "192.163.0.1")
    private String ip;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING)
    @Schema(description = "Дата и время, когда был совершен запрос к эндпоинту (в формате \"yyyy-MM-dd HH:mm:ss\")",
            example = "2022-09-06 11:00:23")
    private LocalDateTime timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
