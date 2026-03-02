package ru.practicum.explore_with_me.ewm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.api.dto.user.NewUserDto;
import ru.practicum.explore_with_me.ewm.api.dto.user.UserDto;
import ru.practicum.explore_with_me.ewm.api.mapper.UserApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера статистики посещений")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService service;

    @MockBean
    private UserApiMapper mapper;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan.petrov@example.com")
                .build();

        testUserDto = UserDto.builder()
                .id(1L)
                .name("Иван Петров")
                .email("ivan.petrov@example.com")
                .build();
    }

    @Test
    @DisplayName("Должен вернуть список пользователей по ids")
    void shouldReturnUsersByIds() throws Exception {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<User> users = List.of(testUser);
        List<UserDto> userDtos = List.of(testUserDto);

        when(service.findByIds(eq(ids), anyInt(), anyInt())).thenReturn(users);
        when(mapper.toDtos(users)).thenReturn(userDtos);

        mockMvc.perform(get("/admin/users")
                        .param("ids", "1", "2", "3")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Иван Петров")))
                .andExpect(jsonPath("$[0].email", is("ivan.petrov@example.com")));

        verify(service, times(1)).findByIds(ids, 0, 10);
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если пользователи не найдены")
    void shouldReturnEmptyListWhenNoUsersFound() throws Exception {
        List<Long> ids = List.of(999L);
        when(service.findByIds(eq(ids), anyInt(), anyInt())).thenReturn(List.of());
        when(mapper.toDtos(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/users")
                        .param("ids", "999")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service, times(1)).findByIds(ids, 0, 10);
    }

    @Test
    @DisplayName("Должен работать без параметра ids")
    void shouldWorkWithoutIds() throws Exception {
        List<User> users = List.of(testUser);
        List<UserDto> userDtos = List.of(testUserDto);

        when(service.findByIds(isNull(), anyInt(), anyInt())).thenReturn(users);
        when(mapper.toDtos(users)).thenReturn(userDtos);

        mockMvc.perform(get("/admin/users")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        verify(service, times(1)).findByIds(isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("Должен использовать значения по умолчанию для from и size")
    void shouldUseDefaultFromAndSize() throws Exception {
        when(service.findByIds(isNull(), eq(0), eq(10))).thenReturn(List.of());
        when(mapper.toDtos(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        verify(service, times(1)).findByIds(isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("Должен вернуть ошибку при пустом имени")
    void shouldReturnErrorWhenNameIsBlank() throws Exception {
        NewUserDto invalidDto = NewUserDto.builder()
                .name("")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при имени меньше 2 символов")
    void shouldReturnErrorWhenNameTooShort() throws Exception {
        NewUserDto invalidDto = NewUserDto.builder()
                .name("A")
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при имени больше 250 символов")
    void shouldReturnErrorWhenNameTooLong() throws Exception {
        String longName = "A".repeat(251);
        NewUserDto invalidDto = NewUserDto.builder()
                .name(longName)
                .email("test@example.com")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при невалидном email")
    void shouldReturnErrorWhenEmailIsInvalid() throws Exception {
        NewUserDto invalidDto = NewUserDto.builder()
                .name("Valid Name")
                .email("not-an-email")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при email меньше 6 символов")
    void shouldReturnErrorWhenEmailTooShort() throws Exception {
        NewUserDto invalidDto = NewUserDto.builder()
                .name("Valid Name")
                .email("a@b.c")
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Должен вернуть ошибку при email больше 254 символов")
    void shouldReturnErrorWhenEmailTooLong() throws Exception {
        String localPart = "a".repeat(250);
        String domain = "b.com";
        String longEmail = localPart + "@" + domain; // будет > 254

        NewUserDto invalidDto = NewUserDto.builder()
                .name("Valid Name")
                .email(longEmail)
                .build();

        mockMvc.perform(post("/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Должен удалить пользователя по id")
    void shouldDeleteUserById() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Должен вернуть 404 при удалении несуществующего пользователя")
    void shouldReturn404WhenUserNotFound() throws Exception {
        doThrow(new NotFoundException("User not found")).when(service).delete(999L);

        mockMvc.perform(delete("/admin/users/999"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).delete(999L);
    }

    @Test
    @DisplayName("Должен обрабатывать ошибки при удалении")
    void shouldHandleDeleteErrors() throws Exception {
        doThrow(new RuntimeException("Database error")).when(service).delete(1L);

        mockMvc.perform(delete("/admin/users/1"))
                .andExpect(status().isInternalServerError());

        verify(service, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Должен корректно маппить список пользователей в DTO")
    void shouldMapUserListToDtoList() throws Exception {
        List<User> users = List.of(testUser);
        List<UserDto> userDtos = List.of(testUserDto);

        when(service.findByIds(any(), anyInt(), anyInt())).thenReturn(users);
        when(mapper.toDtos(users)).thenReturn(userDtos);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk());

        verify(mapper, times(1)).toDtos(users);
    }
}
