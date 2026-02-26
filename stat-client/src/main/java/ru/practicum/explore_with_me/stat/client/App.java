package ru.practicum.explore_with_me.stat.client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;

import java.time.LocalDateTime;

@SpringBootApplication
public class App {
    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(App.class, args);
        StatClient client = applicationContext.getBean(StatClient.class);
        EndpointHit dto = new EndpointHit();
        dto.setApp("ewm-main-service");
        dto.setUri("/events/1");
        dto.setIp("192.163.0.1");
        dto.setTimestamp(LocalDateTime.now());
        client.getStats(LocalDateTime.now().minusMinutes(5), LocalDateTime.now(), null, null);
    }
}
