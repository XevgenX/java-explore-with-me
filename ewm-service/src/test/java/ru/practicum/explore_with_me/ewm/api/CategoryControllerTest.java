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
import ru.practicum.explore_with_me.ewm.api.dto.category.CategoryDto;
import ru.practicum.explore_with_me.ewm.api.dto.category.NewCategoryDto;
import ru.practicum.explore_with_me.ewm.api.mapper.CategoryApiMapper;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;
import ru.practicum.explore_with_me.ewm.domain.service.CategoryService;

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
@DisplayName("Тесты контроллера категорий")
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService service;

    @MockBean
    private CategoryApiMapper mapper;

    private Category testCategory;
    private NewCategory testNewCategory;
    private CategoryDto testCategoryDto;
    private NewCategoryDto testNewCategoryDto;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        testCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("Концерты")
                .build();

        testNewCategoryDto = NewCategoryDto.builder()
                .name("Новая категория")
                .build();
        testNewCategory = NewCategory.builder().name("Новая категория").build();
    }

    // ========================= GET /categories =========================
    @Test
    @DisplayName("GET /categories - должен вернуть список всех категорий")
    void findAll_ShouldReturnListOfCategories() throws Exception {
        List<Category> categories = List.of(testCategory);
        List<CategoryDto> categoryDtos = List.of(testCategoryDto);

        when(service.findAll(anyInt(), anyInt())).thenReturn(categories);
        when(mapper.toDtos(categories)).thenReturn(categoryDtos);

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].name", is("Концерты")));

        verify(service, times(1)).findAll(0, 10);
    }

    @Test
    @DisplayName("GET /categories - должен вернуть пустой список, если категории не найдены")
    void findAll_ShouldReturnEmptyListWhenNoCategories() throws Exception {
        when(service.findAll(anyInt(), anyInt())).thenReturn(List.of());
        when(mapper.toDtos(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(service, times(1)).findAll(0, 10);
    }

    @Test
    @DisplayName("GET /categories - должен использовать значения по умолчанию для from и size")
    void findAll_ShouldUseDefaultFromAndSize() throws Exception {
        when(service.findAll(eq(0), eq(10))).thenReturn(List.of());
        when(mapper.toDtos(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

        verify(service, times(1)).findAll(0, 10);
    }

    @Test
    @DisplayName("GET /categories - должен работать с нулевыми значениями from и size")
    void findAll_ShouldWorkWithZeroFromAndSize() throws Exception {
        mockMvc.perform(get("/categories")
                        .param("from", "0")
                        .param("size", "0"))
                .andExpect(status().isOk());

        verify(service, times(1)).findAll(0, 0);
    }

    // ========================= GET /categories/{id} =========================
    @Test
    @DisplayName("GET /categories/{id} - должен вернуть категорию по id")
    void findById_ShouldReturnCategory() throws Exception {
        when(service.findById(1L)).thenReturn(testCategory);
        when(mapper.toDto(testCategory)).thenReturn(testCategoryDto);

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерты")));

        verify(service, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /categories/{id} - должен вернуть 404 при несуществующем id")
    void findById_ShouldReturn404WhenCategoryNotFound() throws Exception {
        when(service.findById(999L)).thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(get("/categories/999"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).findById(999L);
    }

    @Test
    @DisplayName("POST /admin/categories - должен создать новую категорию")
    void create_ShouldCreateCategory() throws Exception {
        when(mapper.toModel(Mockito.any(NewCategoryDto.class))).thenReturn(testNewCategory);
        when(service.create(Mockito.any(NewCategory.class))).thenReturn(testCategory);
        when(mapper.toDto(testCategory)).thenReturn(testCategoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto))
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/categories/1")))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Концерты")));

        verify(service, times(1)).create(Mockito.any(NewCategory.class));
        verify(mapper, times(1)).toModel(Mockito.any(NewCategoryDto.class));
        verify(mapper, times(1)).toDto(testCategory);
    }

    @Test
    @DisplayName("POST /admin/categories - должен вернуть ошибку при пустом имени")
    void create_ShouldReturnErrorWhenNameIsBlank() throws Exception {
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(service, never()).create(any());
    }

    @Test
    @DisplayName("POST /admin/categories - должен вернуть ошибку при имени меньше 1 символа")
    void create_ShouldReturnErrorWhenNameTooShort() throws Exception {
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/categories - должен вернуть ошибку при имени больше 50 символов")
    void create_ShouldReturnErrorWhenNameTooLong() throws Exception {
        String longName = "A".repeat(51);
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name(longName)
                .build();

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /admin/categories - должен вернуть 409 при дубликате имени")
    void create_ShouldReturn409WhenNameNotUnique() throws Exception {
        when(mapper.toModel(Mockito.any(NewCategoryDto.class))).thenReturn(testNewCategory);
        when(service.create(Mockito.any(NewCategory.class)))
                .thenThrow(new DataIntegrityViolationException("Category name must be unique"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto)))
                .andExpect(status().isConflict());

        verify(service, times(1)).create(Mockito.any(NewCategory.class));
    }

    @Test
    @DisplayName("PATCH /admin/categories/{id} - должен обновить категорию")
    void update_ShouldUpdateCategory() throws Exception {
        Category existingCategory = Category.builder()
                .id(1L)
                .name("Старое название")
                .build();

        Category updatedCategory = Category.builder()
                .id(1L)
                .name("Новое название")
                .build();

        CategoryDto updatedCategoryDto = CategoryDto.builder()
                .id(1L)
                .name("Новое название")
                .build();

        NewCategoryDto updateDto = NewCategoryDto.builder()
                .name("Новое название")
                .build();

        when(service.findById(1L)).thenReturn(existingCategory);
        when(service.update(Mockito.any(Category.class))).thenReturn(updatedCategory);
        when(mapper.toDto(updatedCategory)).thenReturn(updatedCategoryDto);

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Новое название")));

        verify(service, times(1)).findById(1L);
        verify(service, times(1)).update(Mockito.any(Category.class));
    }

    @Test
    @DisplayName("PATCH /admin/categories/{id} - должен вернуть 404 при обновлении несуществующей категории")
    void update_ShouldReturn404WhenCategoryNotFound() throws Exception {
        when(service.findById(999L)).thenThrow(new NotFoundException("Category not found"));

        mockMvc.perform(patch("/admin/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto)))
                .andExpect(status().isNotFound());

        verify(service, times(1)).findById(999L);
        verify(service, never()).update(any());
    }

    @Test
    @DisplayName("PATCH /admin/categories/{id} - должен вернуть 409 при дубликате имени")
    void update_ShouldReturn409WhenNameNotUnique() throws Exception {
        when(service.findById(1L)).thenReturn(testCategory);
        when(service.update(Mockito.any(Category.class)))
                .thenThrow(new DataIntegrityViolationException("Category name must be unique"));

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto)))
                .andExpect(status().isConflict());

        verify(service, times(1)).findById(1L);
        verify(service, times(1)).update(Mockito.any(Category.class));
    }

    @Test
    @DisplayName("PATCH /admin/categories/{id} - должен вернуть ошибку при пустом имени")
    void update_ShouldReturnErrorWhenNameIsBlank() throws Exception {
        NewCategoryDto invalidDto = NewCategoryDto.builder()
                .name("")
                .build();

        mockMvc.perform(patch("/admin/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());

        verify(service, never()).update(any());
    }

    // ========================= DELETE /admin/categories/{id} =========================
    @Test
    @DisplayName("DELETE /admin/categories/{id} - должен удалить категорию")
    void deleteById_ShouldDeleteCategory() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isNoContent());

        verify(service, times(1)).delete(1L);
    }

    @Test
    @DisplayName("DELETE /admin/categories/{id} - должен вернуть 404 при удалении несуществующей категории")
    void deleteById_ShouldReturn404WhenCategoryNotFound() throws Exception {
        doThrow(new NotFoundException("Category not found")).when(service).delete(999L);

        mockMvc.perform(delete("/admin/categories/999"))
                .andExpect(status().isNotFound());

        verify(service, times(1)).delete(999L);
    }

    @Test
    @DisplayName("DELETE /admin/categories/{id} - должен вернуть 409 при удалении категории с событиями")
    void deleteById_ShouldReturn409WhenCategoryHasEvents() throws Exception {
        doThrow(new DataIntegrityViolationException("Category has events"))
                .when(service).delete(1L);

        mockMvc.perform(delete("/admin/categories/1"))
                .andExpect(status().isConflict());

        verify(service, times(1)).delete(1L);
    }

    @Test
    @DisplayName("Должен корректно маппить NewCategoryDto в модель")
    void shouldMapNewCategoryDtoToModel() throws Exception {
        when(mapper.toModel(Mockito.any(NewCategoryDto.class))).thenReturn(testNewCategory);
        when(service.create(Mockito.any(NewCategory.class))).thenReturn(testCategory);
        when(mapper.toDto(testCategory)).thenReturn(testCategoryDto);

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto)))
                .andExpect(status().isCreated());

        verify(mapper, times(1)).toModel(Mockito.any(NewCategoryDto.class));
        verify(mapper, times(1)).toDto(testCategory);
    }

    @Test
    @DisplayName("Должен корректно маппить список категорий в DTO")
    void shouldMapCategoryListToDtoList() throws Exception {
        List<Category> categories = List.of(testCategory);
        List<CategoryDto> categoryDtos = List.of(testCategoryDto);

        when(service.findAll(anyInt(), anyInt())).thenReturn(categories);
        when(mapper.toDtos(categories)).thenReturn(categoryDtos);

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk());

        verify(mapper, times(1)).toDtos(categories);
    }

    // ========================= Тесты обработки ошибок =========================
    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке в GET /categories")
    void shouldReturn500OnUnexpectedErrorInFindAll() throws Exception {
        when(service.findAll(anyInt(), anyInt()))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке в GET /categories/{id}")
    void shouldReturn500OnUnexpectedErrorInFindById() throws Exception {
        when(service.findById(1L))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(get("/categories/1"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("Должен вернуть 500 при неожиданной ошибке в POST /admin/categories")
    void shouldReturn500OnUnexpectedErrorInCreate() throws Exception {
        when(mapper.toModel(Mockito.any(NewCategoryDto.class))).thenReturn(testNewCategory);
        when(service.create(Mockito.any(NewCategory.class)))
                .thenThrow(new RuntimeException("Unexpected database error"));

        mockMvc.perform(post("/admin/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testNewCategoryDto)))
                .andExpect(status().isInternalServerError());
    }
}
