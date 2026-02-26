package ru.practicum.explore_with_me.ewm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory;
import ru.practicum.explore_with_me.ewm.persistence.mapper.CategoryPersistenceMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({CategoryDao.class, CategoryPersistenceMapper.class})
@EnableJpaRepositories(basePackages = "ru.practicum.explore_with_me.ewm.persistence.repository")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для категорий")
public class CategoryDaoTest {

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в правильном порядке (из-за внешних ключей)
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM category");

        // Сбрасываем автоинкремент для H2
        jdbcTemplate.execute("ALTER TABLE category ALTER COLUMN id RESTART WITH 1");
    }

    private void insertTestCategory(String name) {
        jdbcTemplate.update(
                "INSERT INTO category (name) VALUES (?)",
                name
        );
    }

    private void insertTestCategories() {
        insertTestCategory("Концерты");
        insertTestCategory("Спорт");
        insertTestCategory("Театр");
        insertTestCategory("Выставки");
        insertTestCategory("Фестивали");
    }

    @Test
    @DisplayName("findAll - должен возвращать пустой список при from больше размера")
    void findAll_ShouldReturnEmptyListWhenFromExceedsSize() {
        // Подготовка
        insertTestCategories(); // 5 категорий

        // Действие
        List<Category> categories = categoryDao.findAll(10, 5);

        // Проверка
        assertThat(categories).isEmpty();
    }

    @Test
    @DisplayName("findAll - должен возвращать пустой список, если категорий нет")
    void findAll_ShouldReturnEmptyListWhenNoCategories() {
        // Действие
        List<Category> categories = categoryDao.findAll(0, 10);

        // Проверка
        assertThat(categories).isEmpty();
    }

    // ==================== Тесты findById ====================
    @Test
    @DisplayName("findById - должен возвращать категорию по ID")
    void findById_ShouldReturnCategory() {
        // Подготовка
        insertTestCategories();

        // Действие
        Category category = categoryDao.findById(1L);

        // Проверка
        assertAll(
                () -> assertThat(category.getId()).isEqualTo(1L),
                () -> assertThat(category.getName()).isEqualTo("Концерты")
        );
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException при несуществующем ID")
    void findById_ShouldThrowNotFoundExceptionWhenCategoryNotFound() {
        // Подготовка
        insertTestCategories();

        // Действие и проверка
        assertThatThrownBy(() -> categoryDao.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Категория не найдена");
    }

    // ==================== Тесты create ====================
    @Test
    @DisplayName("create - должен создавать новую категорию")
    void create_ShouldCreateCategory() {
        // Подготовка
        NewCategory newCategory = NewCategory.builder()
                .name("Новая категория")
                .build();

        // Действие
        Category created = categoryDao.create(newCategory);

        // Проверка возвращенного объекта
        assertAll(
                () -> assertThat(created.getId()).isNotNull(),
                () -> assertThat(created.getName()).isEqualTo("Новая категория")
        );

        // Проверка, что категория действительно сохранена в БД
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category WHERE name = ?",
                Integer.class,
                "Новая категория"
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("create - должен создавать категорию с правильными данными в БД")
    void create_ShouldSaveCorrectDataInDatabase() {
        // Подготовка
        NewCategory newCategory = NewCategory.builder()
                .name("Тестовая категория")
                .build();

        // Действие
        Category created = categoryDao.create(newCategory);

        // Проверка данных в БД
        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM category WHERE id = ?",
                String.class,
                created.getId()
        );

        assertThat(name).isEqualTo("Тестовая категория");
    }

    @Test
    @DisplayName("create - должен генерировать ID автоматически")
    void create_ShouldGenerateIdAutomatically() {
        // Подготовка
        NewCategory cat1 = NewCategory.builder().name("Категория 1").build();
        NewCategory cat2 = NewCategory.builder().name("Категория 2").build();

        // Действие
        Category created1 = categoryDao.create(cat1);
        Category created2 = categoryDao.create(cat2);

        // Проверка
        assertAll(
                () -> assertThat(created1.getId()).isEqualTo(1L),
                () -> assertThat(created2.getId()).isEqualTo(2L)
        );
    }

    @Test
    @DisplayName("create - должен выбрасывать исключение при дубликате имени")
    void create_ShouldThrowExceptionWhenNameNotUnique() {
        // Подготовка
        NewCategory category1 = NewCategory.builder().name("Уникальная категория").build();
        categoryDao.create(category1);

        NewCategory category2 = NewCategory.builder().name("Уникальная категория").build();

        // Действие и проверка
        assertThatThrownBy(() -> categoryDao.create(category2))
                .isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    // ==================== Тесты update ====================
    @Test
    @DisplayName("update - должен обновлять существующую категорию")
    void update_ShouldUpdateCategory() {
        // Подготовка
        insertTestCategories();
        Category categoryToUpdate = categoryDao.findById(1L);
        categoryToUpdate.setName("Обновленные концерты");

        // Действие
        Category updated = categoryDao.update(categoryToUpdate);

        // Проверка
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(1L),
                () -> assertThat(updated.getName()).isEqualTo("Обновленные концерты")
        );

        // Проверка в БД
        String nameInDb = jdbcTemplate.queryForObject(
                "SELECT name FROM category WHERE id = 1",
                String.class
        );
        assertThat(nameInDb).isEqualTo("Обновленные концерты");
    }

    @Test
    @DisplayName("update - должен обновлять только указанные поля")
    void update_ShouldUpdateOnlySpecifiedFields() {
        // Подготовка
        insertTestCategories();
        Category original = categoryDao.findById(1L);

        Category categoryToUpdate = Category.builder()
                .id(original.getId())
                .name("Новое название")
                .build();

        // Действие
        Category updated = categoryDao.update(categoryToUpdate);

        // Проверка
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(1L),
                () -> assertThat(updated.getName()).isEqualTo("Новое название")
        );
    }

    @Test
    @DisplayName("update - должен выбрасывать исключение при дубликате имени")
    void update_ShouldThrowExceptionWhenNameNotUnique() {
        // Подготовка
        insertTestCategories(); // Концерты (id=1), Спорт (id=2)

        Category categoryToUpdate = categoryDao.findById(1L);
        categoryToUpdate.setName("Спорт"); // Пытаемся изменить на имя, которое уже существует

        // Действие и проверка
        assertThatThrownBy(() -> categoryDao.update(categoryToUpdate))
                .isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    // ==================== Тесты delete ====================
    @Test
    @DisplayName("delete - должен удалять существующую категорию")
    void delete_ShouldDeleteExistingCategory() {
        // Подготовка
        insertTestCategories();

        // Действие
        categoryDao.delete(1L);

        // Проверка
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category WHERE id = 1",
                Integer.class
        );
        assertThat(count).isEqualTo(0);

        // Проверка, что остальные категории не удалились
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category",
                Integer.class
        );
        assertThat(totalCount).isEqualTo(4);
    }

    @Test
    @DisplayName("delete - не должен выбрасывать исключение при удалении несуществующей категории")
    void delete_ShouldNotThrowExceptionWhenDeletingNonExistingCategory() {
        // Подготовка
        insertTestCategories();

        // Действие (не должно быть исключения)
        categoryDao.delete(999L);

        // Проверка, что количество категорий не изменилось
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM category",
                Integer.class
        );
        assertThat(count).isEqualTo(5);
    }

    // ==================== Интеграционные тесты ====================
    @Test
    @DisplayName("Интеграционный тест: создание, поиск, обновление и удаление")
    void integrationTest_CreateFindUpdateAndDelete() {
        // 1. Создание категории
        NewCategory newCategory = NewCategory.builder()
                .name("Интеграционная категория")
                .build();

        Category created = categoryDao.create(newCategory);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Интеграционная категория");

        // 2. Поиск по ID
        Category found = categoryDao.findById(created.getId());
        assertThat(found.getName()).isEqualTo("Интеграционная категория");

        // 3. Поиск в списке
        List<Category> allCategories = categoryDao.findAll(0, 10);
        assertThat(allCategories).hasSize(1);

        // 4. Обновление
        found.setName("Обновленная интеграционная категория");
        Category updated = categoryDao.update(found);
        assertThat(updated.getName()).isEqualTo("Обновленная интеграционная категория");

        // 5. Проверка обновления в БД
        Category afterUpdate = categoryDao.findById(created.getId());
        assertThat(afterUpdate.getName()).isEqualTo("Обновленная интеграционная категория");

        // 6. Удаление
        categoryDao.delete(created.getId());

        // 7. Проверка удаления
        assertThatThrownBy(() -> categoryDao.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);

        List<Category> afterDelete = categoryDao.findAll(0, 10);
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @DisplayName("Интеграционный тест: работа с несколькими категориями")
    void integrationTest_MultipleCategories() {
        // Создаем несколько категорий
        Category cat1 = categoryDao.create(NewCategory.builder().name("Категория A").build());
        Category cat2 = categoryDao.create(NewCategory.builder().name("Категория B").build());
        Category cat3 = categoryDao.create(NewCategory.builder().name("Категория C").build());

        // Проверяем пагинацию
        List<Category> firstPage = categoryDao.findAll(0, 2);
        assertThat(firstPage).hasSize(2);
        assertThat(firstPage.get(0).getName()).isEqualTo("Категория A");
        assertThat(firstPage.get(1).getName()).isEqualTo("Категория B");

        List<Category> secondPage = categoryDao.findAll(2, 2);
        assertThat(secondPage).hasSize(1);
        assertThat(secondPage.get(0).getName()).isEqualTo("Категория C");

        // Обновляем одну категорию
        cat2.setName("Категория B (обновленная)");
        categoryDao.update(cat2);

        // Проверяем обновление
        Category updated = categoryDao.findById(cat2.getId());
        assertThat(updated.getName()).isEqualTo("Категория B (обновленная)");

        // Удаляем одну категорию
        categoryDao.delete(cat1.getId());

        // Проверяем финальное состояние
        List<Category> finalList = categoryDao.findAll(0, 10);
        assertThat(finalList).hasSize(2);
        assertThat(finalList.stream().map(Category::getName))
                .containsExactlyInAnyOrder("Категория B (обновленная)", "Категория C");
    }
}
