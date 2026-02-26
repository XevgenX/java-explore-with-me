package ru.practicum.explore_with_me.ewm.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.CompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.NewCompilationDto;
import ru.practicum.explore_with_me.ewm.api.dto.compilation.UpdateCompilationRequestDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CompilationApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.domain.service.CompilationService;
import ru.practicum.explore_with_me.ewm.domain.service.EventService;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Тесты контроллера подборок")
public class CompilationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CompilationService compilationService;

    @MockBean
    private EventService eventService;

    @MockBean
    private CompilationApiMapper mapper;

    private Compilation testCompilation;
    private NewCompilation testNewCompilation;
    private UpdatingCompilation testUpdatingCompilation;
    private CompilationDto testCompilationDto;
    private NewCompilationDto testNewCompilationDto;
    private UpdateCompilationRequestDto testUpdateCompilationDto;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        var testCategory = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        var testLocation = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        var testInitiator = User.builder()
                .id(1L)
                .name("Инициатор")
                .email("initiator@example.com")
                .build();

        testEvent = Event.builder()
                .id(1L)
                .title("Концерт")
                .annotation("Аннотация концерта")
                .description("Описание концерта")
                .initiator(testInitiator)
                .category(testCategory)
                .location(testLocation)
                .eventDate(LocalDateTime.now().plusDays(10))
                .createdOn(LocalDateTime.now())
                .publishedOn(LocalDateTime.now())
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .state(EventState.PUBLISHED)
                .views(0L)
                .confirmedRequests(0)
                .build();

        testCompilation = Compilation.builder()
                .id(1L)
                .title("Летние концерты")
                .pined(true)
                .events(List.of(testEvent))
                .build();

        testNewCompilation = NewCompilation.builder()
                .title("Летние концерты")
                .pined(true)
                .events(List.of(testEvent))
                .build();

        testUpdatingCompilation = UpdatingCompilation.builder()
                .id(1L)
                .title("Летние концерты")
                .pined(true)
                .events(List.of(testEvent))
                .build();

        testCompilationDto = CompilationDto.builder()
                .id(1L)
                .title("Летние концерты")
                .pinned(true)
                .events(List.of())
                .build();

        testNewCompilationDto = NewCompilationDto.builder()
                .title("Новая подборка")
                .pinned(true)
                .events(List.of(1L, 2L))
                .build();

        testUpdateCompilationDto = UpdateCompilationRequestDto.builder()
                .title("Обновленная подборка")
                .pinned(false)
                .events(List.of(1L))
                .build();
    }

    // ==================== GET /compilations ====================
    @Test
    @DisplayName("GET /compilations - должен вернуть список подборок")
    void findAll_ShouldReturnCompilations() throws Exception {
        List<Compilation> compilations = List.of(testCompilation);
        List<CompilationDto> compilationDtos = List.of(testCompilationDto);

        Mockito.when(compilationService.findAll(Mockito.isNull(), eq(0), eq(10)))
                .thenReturn(compilations);
        Mockito.when(mapper.toDtoList(compilations)).thenReturn(compilationDtos);

        mockMvc.perform(get("/compilations")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].title", is("Летние концерты")));

        Mockito.verify(compilationService, Mockito.times(1))
                .findAll(Mockito.isNull(), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /compilations - должен фильтровать по pinned")
    void findAll_ShouldFilterByPinned() throws Exception {
        Mockito.when(compilationService.findAll(eq(true), eq(0), eq(10)))
                .thenReturn(List.of(testCompilation));
        Mockito.when(mapper.toDtoList(anyList())).thenReturn(List.of(testCompilationDto));

        mockMvc.perform(get("/compilations")
                        .param("pinned", "true")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        Mockito.verify(compilationService, Mockito.times(1))
                .findAll(eq(true), eq(0), eq(10));
    }

    @Test
    @DisplayName("GET /compilations - должен использовать значения по умолчанию")
    void findAll_ShouldUseDefaultValues() throws Exception {
        Mockito.when(compilationService.findAll(Mockito.isNull(), eq(0), eq(10)))
                .thenReturn(List.of());
        Mockito.when(mapper.toDtoList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        Mockito.verify(compilationService, Mockito.times(1))
                .findAll(Mockito.isNull(), eq(0), eq(10));
    }

    // ==================== GET /compilations/{compId} ====================
    @Test
    @DisplayName("GET /compilations/{compId} - должен вернуть подборку по id")
    void findById_ShouldReturnCompilation() throws Exception {
        Mockito.when(compilationService.findById(1L)).thenReturn(testCompilation);
        Mockito.when(mapper.toDto(testCompilation)).thenReturn(testCompilationDto);

        mockMvc.perform(get("/compilations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Летние концерты")));

        Mockito.verify(compilationService, Mockito.times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /compilations/{compId} - должен вернуть 404 при несуществующей подборке")
    void findById_ShouldReturn404WhenCompilationNotFound() throws Exception {
        Mockito.when(compilationService.findById(999L))
                .thenThrow(new NotFoundException("Compilation not found"));

        mockMvc.perform(get("/compilations/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /admin/compilations - должен вернуть ошибку при пустом заголовке")
    void create_ShouldReturnErrorWhenTitleIsBlank() throws Exception {
        NewCompilationDto invalidDto = NewCompilationDto.builder()
                .title("")
                .pinned(true)
                .build();

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        Mockito.verify(compilationService, never()).create(any());
    }

    @Test
    @DisplayName("POST /admin/compilations - должен вернуть 409 при дубликате заголовка")
    void create_ShouldReturn409WhenTitleNotUnique() throws Exception {
        Mockito.when(mapper.toNewModel(Mockito.any(NewCompilationDto.class), anyList()))
                .thenReturn(testNewCompilation);
        Mockito.when(compilationService.create(Mockito.any(NewCompilation.class)))
                .thenThrow(new DataIntegrityViolationException("Title must be unique"));

        mockMvc.perform(post("/admin/compilations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCompilationDto)))
                .andExpect(status().isConflict());
    }

    // ==================== PATCH /admin/compilations/{compId} ====================
    @Test
    @DisplayName("PATCH /admin/compilations/{compId} - должен обновить подборку")
    void update_ShouldUpdateCompilation() throws Exception {
        List<Event> events = List.of(testEvent);
        Compilation updatedCompilation = Compilation.builder()
                .id(1L)
                .title("Обновленная подборка")
                .pined(false)
                .events(events)
                .build();

        CompilationDto updatedDto = CompilationDto.builder()
                .id(1L)
                .title("Обновленная подборка")
                .pinned(false)
                .events(List.of())
                .build();

        Mockito.when(eventService.findById(1L)).thenReturn(testEvent);
        Mockito.when(mapper.toUpdateModel(eq(testUpdateCompilationDto), eq(1L), anyList()))
                .thenReturn(testUpdatingCompilation);
        Mockito.when(compilationService.update(testUpdatingCompilation)).thenReturn(updatedCompilation);
        Mockito.when(mapper.toDto(updatedCompilation)).thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testUpdateCompilationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Обновленная подборка")))
                .andExpect(jsonPath("$.pinned", is(false)));

        Mockito.verify(eventService, Mockito.times(1)).findById(1L);
        Mockito.verify(compilationService, Mockito.times(1)).update(testUpdatingCompilation);
    }

    @Test
    @DisplayName("PATCH /admin/compilations/{compId} - должен обновить без изменений событий")
    void update_ShouldUpdateWithoutEvents() throws Exception {
        UpdateCompilationRequestDto dtoWithoutEvents = UpdateCompilationRequestDto.builder()
                .title("Только заголовок")
                .pinned(true)
                .events(null)
                .build();

        Compilation updatedCompilation = Compilation.builder()
                .id(1L)
                .title("Только заголовок")
                .pined(true)
                .events(List.of(testEvent))
                .build();

        CompilationDto updatedDto = CompilationDto.builder()
                .id(1L)
                .title("Только заголовок")
                .pinned(true)
                .events(List.of())
                .build();

        Mockito.when(mapper.toUpdateModel(eq(dtoWithoutEvents), eq(1L), anyList()))
                .thenReturn(testUpdatingCompilation);
        Mockito.when(compilationService.update(testUpdatingCompilation)).thenReturn(updatedCompilation);
        Mockito.when(mapper.toDto(updatedCompilation)).thenReturn(updatedDto);

        mockMvc.perform(patch("/admin/compilations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoWithoutEvents)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title", is("Только заголовок")));

        Mockito.verify(eventService, never()).findById(anyLong());
    }

    @Test
    @DisplayName("DELETE /admin/compilations/{compId} - должен удалить подборку")
    void delete_ShouldDeleteCompilation() throws Exception {
        Mockito.doNothing().when(compilationService).delete(1L);

        mockMvc.perform(delete("/admin/compilations/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(compilationService, Mockito.times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /admin/compilations/{compId} - должен вернуть 404 при удалении несуществующей подборки")
    void delete_ShouldReturn404WhenCompilationNotFound() throws Exception {
        Mockito.doThrow(new NotFoundException("Compilation not found"))
                .when(compilationService).delete(999L);

        mockMvc.perform(delete("/admin/compilations/999"))
                .andExpect(status().isNotFound());

        Mockito.verify(compilationService, Mockito.times(1)).delete(999L);
    }

    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке")
    void shouldReturn500OnUnexpectedError() throws Exception {
        Mockito.when(compilationService.findAll(any(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Unexpected error"));

        mockMvc.perform(get("/compilations"))
                .andExpect(status().isInternalServerError());
    }
}