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
import ru.practicum.explore_with_me.ewm.domain.model.compilation.Compilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.NewCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.compilation.UpdatingCompilation;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewLocation;
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.persistence.mapper.*;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({
        CompilationDao.class,
        CompilationPersistenceMapper.class,
        EventDao.class,
        EventPersistenceMapper.class,
        UserDao.class,
        UserPersistenceMapper.class,
        CategoryDao.class,
        CategoryPersistenceMapper.class
})
@EnableJpaRepositories(basePackages = "ru.practicum.explore_with_me.ewm.persistence.repository")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для подборок")
public class CompilationDaoTest {

    @Autowired
    private CompilationDao compilationDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User initiator;
    private Category category;
    private Location location;
    private NewLocation newLocation;
    private Event event1;
    private Event event2;
    private NewCompilation newCompilation;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в правильном порядке
        jdbcTemplate.execute("DELETE FROM compilation_events");
        jdbcTemplate.execute("DELETE FROM compilations");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM locations");

        // Сбрасываем автоинкремент для H2
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE category ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE locations ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE compilations ALTER COLUMN id RESTART WITH 1");

        // Создаем пользователя
        initiator = userDao.create(NewUser.builder()
                .name("Инициатор")
                .email("initiator@example.com")
                .build());

        // Создаем категорию
        category = categoryDao.create(ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory.builder()
                .name("Концерты")
                .build());

        // Создаем локацию
        location = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        newLocation = NewLocation.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        jdbcTemplate.update(
                "INSERT INTO locations (lat, lon) VALUES (?, ?)",
                location.getLat(), location.getLon()
        );

        Long locationId = jdbcTemplate.queryForObject(
                "SELECT id FROM locations ORDER BY id DESC LIMIT 1",
                Long.class
        );
        location.setId(locationId);

        // Создаем события
        NewEvent newEvent1 = NewEvent.builder()
                .title("Рок-концерт")
                .annotation("Лучшие рок-хиты")
                .description("Полное описание концерта")
                .initiator(initiator)
                .category(category)
                .location(newLocation)
                .eventDate(LocalDateTime.now().plusDays(10))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .build();
        event1 = eventDao.create(newEvent1);

        NewEvent newEvent2 = NewEvent.builder()
                .title("Джазовый концерт")
                .annotation("Джазовые стандарты")
                .description("Описание джазового концерта")
                .initiator(initiator)
                .category(category)
                .location(newLocation)
                .eventDate(LocalDateTime.now().plusDays(20))
                .paid(true)
                .participantLimit(50)
                .requestModeration(true)
                .build();
        event2 = eventDao.create(newEvent2);

        // Создаем новую подборку
        newCompilation = NewCompilation.builder()
                .title("Лучшие концерты")
                .pined(true)
                .events(List.of(event1, event2))
                .build();
    }

    private Compilation createTestCompilation(String title, Boolean pinned, List<Event> events) {
        NewCompilation compilation = NewCompilation.builder()
                .title(title)
                .pined(pinned)
                .events(events)
                .build();
        return compilationDao.create(compilation);
    }

    private List<Compilation> createMultipleCompilations() {
        Compilation comp1 = createTestCompilation("Подборка 1", true, List.of(event1));
        Compilation comp2 = createTestCompilation("Подборка 2", true, List.of(event2));
        Compilation comp3 = createTestCompilation("Подборка 3", false, List.of(event1, event2));
        Compilation comp4 = createTestCompilation("Подборка 4", false, List.of());

        return List.of(comp1, comp2, comp3, comp4);
    }

    // ==================== Тесты findAll ====================
    @Test
    @DisplayName("findAll - должен возвращать все подборки с пагинацией")
    void findAll_ShouldReturnAllCompilations() {
        // Подготовка
        createMultipleCompilations();

        // Действие
        List<Compilation> compilations = compilationDao.findAll(null, 0, 10);

        // Проверка
        assertThat(compilations).hasSize(4);
    }

    @Test
    @DisplayName("findAll - должен фильтровать по pinned")
    void findAll_ShouldFilterByPinned() {
        // Подготовка
        createMultipleCompilations();

        // Действие
        List<Compilation> pinnedCompilations = compilationDao.findAll(true, 0, 10);

        // Проверка
        assertThat(pinnedCompilations).hasSize(2); // Подборка 1 и 2
        assertThat(pinnedCompilations.stream().map(Compilation::getTitle))
                .containsExactlyInAnyOrder("Подборка 1", "Подборка 2");

        // Действие
        List<Compilation> unpinnedCompilations = compilationDao.findAll(false, 0, 10);

        // Проверка
        assertThat(unpinnedCompilations).hasSize(2); // Подборка 3 и 4
        assertThat(unpinnedCompilations.stream().map(Compilation::getTitle))
                .containsExactlyInAnyOrder("Подборка 3", "Подборка 4");
    }

    @Test
    @DisplayName("findAll - должен учитывать пагинацию")
    void findAll_ShouldRespectPagination() {
        // Подготовка
        createMultipleCompilations();

        // Действие - первая страница
        List<Compilation> firstPage = compilationDao.findAll(null, 0, 2);

        // Проверка
        assertThat(firstPage).hasSize(2);

        // Действие - вторая страница
        List<Compilation> secondPage = compilationDao.findAll(null, 2, 2);

        // Проверка
        assertThat(secondPage).hasSize(2);
    }

    @Test
    @DisplayName("findAll - должен возвращать пустой список если подборок нет")
    void findAll_ShouldReturnEmptyListWhenNoCompilations() {
        // Действие
        List<Compilation> compilations = compilationDao.findAll(null, 0, 10);

        // Проверка
        assertThat(compilations).isEmpty();
    }

    // ==================== Тесты findById ====================
    @Test
    @DisplayName("findById - должен возвращать подборку по ID с событиями")
    void findById_ShouldReturnCompilationWithEvents() {
        // Подготовка
        Compilation created = compilationDao.create(newCompilation);

        // Действие
        Compilation found = compilationDao.findById(created.getId());

        // Проверка
        assertAll(
                () -> assertThat(found.getId()).isEqualTo(created.getId()),
                () -> assertThat(found.getTitle()).isEqualTo("Лучшие концерты"),
                () -> assertThat(found.getPined()).isTrue(),
                () -> assertThat(found.getEvents()).hasSize(2),
                () -> assertThat(found.getEvents().stream().map(Event::getTitle))
                        .containsExactlyInAnyOrder("Рок-концерт", "Джазовый концерт")
        );
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException при несуществующем ID")
    void findById_ShouldThrowNotFoundExceptionWhenCompilationNotFound() {
        assertThatThrownBy(() -> compilationDao.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Подборка не найдена");
    }

    // ==================== Тесты create ====================
    @Test
    @DisplayName("create - должен создавать новую подборку с событиями")
    void create_ShouldCreateCompilationWithEvents() {
        // Действие
        Compilation created = compilationDao.create(newCompilation);

        // Проверка возвращенного объекта
        assertAll(
                () -> assertThat(created.getId()).isNotNull(),
                () -> assertThat(created.getTitle()).isEqualTo("Лучшие концерты"),
                () -> assertThat(created.getPined()).isTrue(),
                () -> assertThat(created.getEvents()).hasSize(2)
        );

        // Проверка в БД
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilations WHERE title = ?",
                Integer.class,
                "Лучшие концерты"
        );
        assertThat(count).isEqualTo(1);

        // Проверка связей
        Integer eventsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilation_events WHERE compilation_id = ?",
                Integer.class,
                created.getId()
        );
        assertThat(eventsCount).isEqualTo(2);
    }

    @Test
    @DisplayName("create - должен создавать подборку без событий")
    void create_ShouldCreateCompilationWithoutEvents() {
        // Подготовка
        NewCompilation emptyCompilation = NewCompilation.builder()
                .title("Пустая подборка")
                .pined(false)
                .events(List.of())
                .build();

        // Действие
        Compilation created = compilationDao.create(emptyCompilation);

        // Проверка
        assertAll(
                () -> assertThat(created.getId()).isNotNull(),
                () -> assertThat(created.getTitle()).isEqualTo("Пустая подборка"),
                () -> assertThat(created.getPined()).isFalse(),
                () -> assertThat(created.getEvents()).isEmpty()
        );

        // Проверка связей
        Integer eventsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilation_events WHERE compilation_id = ?",
                Integer.class,
                created.getId()
        );
        assertThat(eventsCount).isEqualTo(0);
    }

    @Test
    @DisplayName("create - должен генерировать ID автоматически")
    void create_ShouldGenerateIdAutomatically() {
        // Действие
        Compilation created1 = compilationDao.create(newCompilation);

        NewCompilation anotherCompilation = NewCompilation.builder()
                .title("Другая подборка")
                .pined(false)
                .events(List.of(event1))
                .build();
        Compilation created2 = compilationDao.create(anotherCompilation);

        // Проверка
        assertAll(
                () -> assertThat(created1.getId()).isEqualTo(1L),
                () -> assertThat(created2.getId()).isEqualTo(2L)
        );
    }

    // ==================== Тесты update ====================
    @Test
    @DisplayName("update - должен обновлять существующую подборку")
    void update_ShouldUpdateCompilation() {
        // Подготовка
        Compilation created = compilationDao.create(newCompilation);

        UpdatingCompilation updatingCompilation = UpdatingCompilation.builder()
                .id(created.getId())
                .title("Обновленная подборка")
                .pined(false)
                .events(List.of(event1))
                .build();

        // Действие
        Compilation updated = compilationDao.update(updatingCompilation);

        // Проверка
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(created.getId()),
                () -> assertThat(updated.getTitle()).isEqualTo("Обновленная подборка"),
                () -> assertThat(updated.getPined()).isFalse(),
                () -> assertThat(updated.getEvents()).hasSize(1),
                () -> assertThat(updated.getEvents().get(0).getTitle()).isEqualTo("Рок-концерт")
        );

        // Проверка в БД
        String titleInDb = jdbcTemplate.queryForObject(
                "SELECT title FROM compilations WHERE id = ?",
                String.class,
                created.getId()
        );
        assertThat(titleInDb).isEqualTo("Обновленная подборка");

        // Проверка связей
        Integer eventsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilation_events WHERE compilation_id = ?",
                Integer.class,
                created.getId()
        );
        assertThat(eventsCount).isEqualTo(1);
    }

    @Test
    @DisplayName("update - должен обновлять только список событий")
    void update_ShouldUpdateOnlyEvents() {
        // Подготовка
        Compilation created = compilationDao.create(newCompilation);

        UpdatingCompilation updatingCompilation = UpdatingCompilation.builder()
                .id(created.getId())
                .events(List.of(event1))
                .build();

        // Действие
        Compilation updated = compilationDao.update(updatingCompilation);

        // Проверка
        assertAll(
                () -> assertThat(updated.getTitle()).isEqualTo("Лучшие концерты"), // не изменилось
                () -> assertThat(updated.getPined()).isTrue(), // не изменилось
                () -> assertThat(updated.getEvents()).hasSize(1),
                () -> assertThat(updated.getEvents().get(0).getTitle()).isEqualTo("Рок-концерт")
        );
    }

    @Test
    @DisplayName("update - должен выбрасывать NotFoundException при обновлении несуществующей подборки")
    void update_ShouldThrowNotFoundExceptionWhenCompilationNotFound() {
        // Подготовка
        UpdatingCompilation updatingCompilation = UpdatingCompilation.builder()
                .id(999L)
                .title("Несуществующая подборка")
                .build();

        // Действие и проверка
        assertThatThrownBy(() -> compilationDao.update(updatingCompilation))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Подборка не найдена");
    }

    // ==================== Тесты delete ====================
    @Test
    @DisplayName("delete - должен удалять существующую подборку")
    void delete_ShouldDeleteExistingCompilation() {
        // Подготовка
        Compilation created = compilationDao.create(newCompilation);

        // Действие
        compilationDao.delete(created.getId());

        // Проверка
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilations WHERE id = ?",
                Integer.class,
                created.getId()
        );
        assertThat(count).isEqualTo(0);

        // Проверка, что связи тоже удалились (CASCADE)
        Integer eventsCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM compilation_events WHERE compilation_id = ?",
                Integer.class,
                created.getId()
        );
        assertThat(eventsCount).isEqualTo(0);
    }

    @Test
    @DisplayName("delete - не должен выбрасывать исключение при удалении несуществующей подборки")
    void delete_ShouldNotThrowExceptionWhenDeletingNonExistingCompilation() {
        // Действие (не должно быть исключения)
        compilationDao.delete(999L);
    }

    // ==================== Интеграционные тесты ====================
    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл подборки")
    void integrationTest_CompilationLifecycle() {
        // 1. Создание подборки
        Compilation created = compilationDao.create(newCompilation);
        assertThat(created.getId()).isNotNull();

        // 2. Поиск по ID
        Compilation found = compilationDao.findById(created.getId());
        assertThat(found.getTitle()).isEqualTo("Лучшие концерты");
        assertThat(found.getEvents()).hasSize(2);

        // 3. Поиск всех подборок
        List<Compilation> allCompilations = compilationDao.findAll(null, 0, 10);
        assertThat(allCompilations).hasSize(1);

        // 4. Обновление подборки
        UpdatingCompilation updatingCompilation = UpdatingCompilation.builder()
                .id(created.getId())
                .title("Обновленная подборка")
                .pined(false)
                .events(List.of(event1))
                .build();
        Compilation updated = compilationDao.update(updatingCompilation);
        assertThat(updated.getTitle()).isEqualTo("Обновленная подборка");
        assertThat(updated.getPined()).isFalse();
        assertThat(updated.getEvents()).hasSize(1);

        // 5. Фильтрация по pinned
        List<Compilation> pinnedCompilations = compilationDao.findAll(true, 0, 10);
        assertThat(pinnedCompilations).isEmpty();

        List<Compilation> unpinnedCompilations = compilationDao.findAll(false, 0, 10);
        assertThat(unpinnedCompilations).hasSize(1);

        // 6. Удаление
        compilationDao.delete(created.getId());

        // 7. Проверка удаления
        assertThatThrownBy(() -> compilationDao.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);

        List<Compilation> afterDelete = compilationDao.findAll(null, 0, 10);
        assertThat(afterDelete).isEmpty();
    }
}