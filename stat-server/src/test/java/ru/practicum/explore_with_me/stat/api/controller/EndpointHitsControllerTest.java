package ru.practicum.explore_with_me.stat.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.stat.api.dto.EndpointHit;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EndpointHitsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void save_whenValidEndpointHit_shouldReturnCreated() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.app").value("ewm-main-service"))
                .andExpect(jsonPath("$.uri").value("/events/1"))
                .andExpect(jsonPath("$.ip").value("192.163.0.1"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void save_whenEndpointHitWithNullApp_shouldReturnBadRequest() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();
        requestDto.setApp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenEndpointHitWithEmptyApp_shouldReturnBadRequest() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();
        requestDto.setApp("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenEndpointHitWithNullUri_shouldReturnBadRequest() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();
        requestDto.setUri(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenEndpointHitWithEmptyUri_shouldReturnBadRequest() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();
        requestDto.setUri("");

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void save_whenEndpointHitWithNullIp_shouldReturnBadRequest() throws Exception {
        EndpointHit requestDto = createValidEndpointHit();
        requestDto.setIp(null);

        mockMvc.perform(post("/hit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    private EndpointHit createValidEndpointHit() {
        EndpointHit dto = new EndpointHit();
        dto.setApp("ewm-main-service");
        dto.setUri("/events/1");
        dto.setIp("192.163.0.1");
        dto.setTimestamp(LocalDateTime.now());
        return dto;
    }
}
