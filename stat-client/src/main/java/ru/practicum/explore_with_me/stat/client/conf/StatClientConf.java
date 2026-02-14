package ru.practicum.explore_with_me.stat.client.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class StatClientConf {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
