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
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewLocation;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.persistence.mapper.EventPersistenceMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({EventDao.class, EventPersistenceMapper.class, CategoryDao.class, UserDao.class})
@EnableJpaRepositories(basePackages = "ru.practicum.explore_with_me.ewm.persistence.repository")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для событий")
public class EventDaoTest {

    @Autowired
    private EventDao eventDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testUser;
    private User testUser2;
    private Category testCategory;
    private Category testCategory2;
    private Location testLocation;
    private NewLocation testNewLocation;
    private NewEvent testNewEvent;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в правильном порядке (из-за внешних ключей)
        jdbcTemplate.execute("DELETE FROM participation_requests");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM locations");

        // Сбрасываем автоинкремент для H2
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE category ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE locations ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN id RESTART WITH 1");

        // Создаем тестовые данные
        testUser = userDao.create(ru.practicum.explore_with_me.ewm.domain.model.user.NewUser.builder()
                .name("Иван Петров")
                .email("ivan@example.com")
                .build());

        testUser2 = userDao.create(ru.practicum.explore_with_me.ewm.domain.model.user.NewUser.builder()
                .name("Мария Сидорова")
                .email("maria@example.com")
                .build());

        testCategory = categoryDao.create(ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory.builder()
                .name("Концерты")
                .build());

        testCategory2 = categoryDao.create(ru.practicum.explore_with_me.ewm.domain.model.category.NewCategory.builder()
                .name("Спорт")
                .build());

        testLocation = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
                .build();

        testNewLocation = NewLocation.builder().lat(55.754167F)
                .lon(37.62F).build();

        // Сохраняем локацию напрямую через JdbcTemplate
        jdbcTemplate.update(
                "INSERT INTO locations (lat, lon) VALUES (?, ?)",
                testLocation.getLat(), testLocation.getLon()
        );

        // Получаем ID локации
        Long locationId = jdbcTemplate.queryForObject(
                "SELECT id FROM locations ORDER BY id DESC LIMIT 1",
                Long.class
        );
        testLocation.setId(locationId);

        testNewEvent = NewEvent.builder()
                .title("Рок-концерт")
                .annotation("Лучшие рок-хиты")
                .description("Полное описание рок-концерта")
                .initiator(testUser)
                .category(testCategory)
                .location(testNewLocation)
                .eventDate(LocalDateTime.now().plusDays(10))
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .build();
    }

    private Event createTestEvent(String title, User initiator, Category category,
                                  EventState state, LocalDateTime eventDate) {
        NewEvent event = NewEvent.builder()
                .title(title)
                .annotation("Аннотация " + title)
                .description("Описание " + title)
                .initiator(initiator)
                .category(category)
                .location(testNewLocation)
                .eventDate(eventDate)
                .paid(true)
                .participantLimit(100)
                .requestModeration(true)
                .build();
        return eventDao.create(event);
    }

    private List<Event> createMultipleTestEvents() {
        LocalDateTime now = LocalDateTime.now();

        Event event1 = createTestEvent("Концерт 1", testUser, testCategory,
                EventState.PUBLISHED, now.plusDays(5));
        Event event2 = createTestEvent("Концерт 2", testUser, testCategory,
                EventState.PUBLISHED, now.plusDays(10));
        Event event3 = createTestEvent("Спорт 1", testUser2, testCategory2,
                EventState.PUBLISHED, now.plusDays(15));
        Event event4 = createTestEvent("Спорт 2", testUser2, testCategory2,
                EventState.WAITING_FOR_PUBLICATION, now.plusDays(20));
        Event event5 = createTestEvent("Театр", testUser, testCategory,
                EventState.CANCEL_REVIEW, now.plusDays(25));

        return List.of(event1, event2, event3, event4, event5);
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException при несуществующем ID")
    void findById_ShouldThrowNotFoundExceptionWhenEventNotFound() {
        // Подготовка
        eventDao.create(testNewEvent);

        // Действие и проверка
        assertThatThrownBy(() -> eventDao.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Такого события не существует");
    }

    @Test
    @DisplayName("create - должен генерировать ID автоматически")
    void create_ShouldGenerateIdAutomatically() {
        // Действие
        Event created1 = eventDao.create(testNewEvent);

        NewEvent anotherEvent = NewEvent.builder()
                .title("Другой концерт")
                .annotation("Аннотация")
                .description("Описание")
                .initiator(testUser)
                .category(testCategory)
                .location(testNewLocation)
                .eventDate(LocalDateTime.now().plusDays(20))
                .paid(false)
                .participantLimit(50)
                .requestModeration(false)
                .build();
        Event created2 = eventDao.create(anotherEvent);

        // Проверка
        assertAll(
                () -> assertThat(created1.getId()).isEqualTo(1L),
                () -> assertThat(created2.getId()).isEqualTo(2L)
        );
    }

    // ==================== Тесты update ====================
    @Test
    @DisplayName("update - должен обновлять существующее событие")
    void update_ShouldUpdateEvent() {
        // Подготовка
        Event created = eventDao.create(testNewEvent);

        created.setTitle("Обновленный концерт");
        created.setAnnotation("Обновленная аннотация");
        created.setState(EventState.PUBLICATION_CANCELED);

        // Действие
        Event updated = eventDao.update(created);

        // Проверка
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(created.getId()),
                () -> assertThat(updated.getTitle()).isEqualTo("Обновленный концерт"),
                () -> assertThat(updated.getAnnotation()).isEqualTo("Обновленная аннотация"),
                () -> assertThat(updated.getState()).isEqualTo(EventState.PUBLICATION_CANCELED)
        );

        // Проверка в БД
        String titleInDb = jdbcTemplate.queryForObject(
                "SELECT title FROM events WHERE id = ?",
                String.class,
                created.getId()
        );
        assertThat(titleInDb).isEqualTo("Обновленный концерт");
    }

    @Test
    @DisplayName("findByFilter (админ) - должен фильтровать по пользователям")
    void findByFilter_Admin_ShouldFilterByUsers() {
        // Подготовка
        createMultipleTestEvents();

        // Действие
        List<Event> events = eventDao.findByFilter(
                List.of(testUser.getId()), null, null, null, null, 0, 10
        );

        // Проверка
        assertThat(events).hasSize(3);
        assertThat(events.stream().map(Event::getInitiator).map(User::getId))
                .allMatch(id -> id.equals(testUser.getId()));
    }

    @Test
    @DisplayName("findByFilter (админ) - должен фильтровать по категориям")
    void findByFilter_Admin_ShouldFilterByCategories() {
        // Подготовка
        createMultipleTestEvents();

        // Действие
        List<Event> events = eventDao.findByFilter(
                null, null, List.of(testCategory2.getId()), null, null, 0, 10
        );

        // Проверка
        assertThat(events).hasSize(2); // Спорт 1, Спорт 2
        assertThat(events.stream().map(Event::getCategory).map(Category::getId))
                .allMatch(id -> id.equals(testCategory2.getId()));
    }

    @Test
    @DisplayName("findByFilter (админ) - должен фильтровать по диапазону дат")
    void findByFilter_Admin_ShouldFilterByDateRange() {
        // Подготовка
        createMultipleTestEvents();
        LocalDateTime now = LocalDateTime.now();

        // Действие
        List<Event> events = eventDao.findByFilter(
                null, null, null, now.plusDays(4), now.plusDays(11), 0, 10
        );

        // Проверка
        assertThat(events).hasSize(2); // Концерт 1 (+5) и Концерт 2 (+10)
    }

    // ==================== Тесты findByFilter (публичная версия) ====================
    @Test
    @DisplayName("findByFilter (публичный) - должен фильтровать по тексту")
    void findByFilter_Public_ShouldFilterByText() {
        // Подготовка
        createMultipleTestEvents();

        // Действие - поиск по тексту в заголовке
        List<Event> events = eventDao.findByFilter(
                "Концерт", null, null, null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(events).hasSize(2);
        assertThat(events.stream().map(Event::getTitle))
                .containsExactlyInAnyOrder("Концерт 1", "Концерт 2");
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен фильтровать по тексту без учета регистра")
    void findByFilter_Public_ShouldFilterByTextCaseInsensitive() {
        // Подготовка
        createMultipleTestEvents();

        // Действие - поиск в нижнем регистре
        List<Event> eventsLower = eventDao.findByFilter(
                "концерт", null, null, null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(eventsLower).hasSize(2);
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен фильтровать по paid")
    void findByFilter_Public_ShouldFilterByPaid() {
        // Подготовка
        createMultipleTestEvents();

        // Действие - только платные (все тестовые платные)
        List<Event> paidEvents = eventDao.findByFilter(
                null, true, null, null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(paidEvents).hasSize(5);

        // Действие - только бесплатные
        List<Event> freeEvents = eventDao.findByFilter(
                null, false, null, null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(freeEvents).isEmpty();
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен фильтровать по категориям")
    void findByFilter_Public_ShouldFilterByCategories() {
        // Подготовка
        createMultipleTestEvents();

        // Действие
        List<Event> events = eventDao.findByFilter(
                null, null, List.of(testCategory.getId()), null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(events).hasSize(3); // Концерт 1, Концерт 2, Театр
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен фильтровать по диапазону дат")
    void findByFilter_Public_ShouldFilterByDateRange() {
        // Подготовка
        createMultipleTestEvents();
        LocalDateTime now = LocalDateTime.now();

        // Действие
        List<Event> events = eventDao.findByFilter(
                null, null, null, now.plusDays(4), now.plusDays(11), null, 0, 10, true
        );

        // Проверка
        assertThat(events).hasSize(2); // Концерт 1 (+5) и Концерт 2 (+10)
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен сортировать по дате")
    void findByFilter_Public_ShouldSortByDate() {
        // Подготовка
        createMultipleTestEvents();

        // Действие - сортировка по дате (возрастание)
        List<Event> events = eventDao.findByFilter(
                null, null, null, null, null, null, 0, 10, true
        );

        // Проверка
        assertThat(events).isSortedAccordingTo(
                (e1, e2) -> e1.getEventDate().compareTo(e2.getEventDate())
        );
    }

    @Test
    @DisplayName("findByFilter (публичный) - должен комбинировать все фильтры")
    void findByFilter_Public_ShouldCombineAllFilters() {
        // Подготовка
        createMultipleTestEvents();
        LocalDateTime now = LocalDateTime.now();

        // Действие - сложный фильтр
        List<Event> events = eventDao.findByFilter(
                "Концерт",  // текст
                true,       // только платные
                List.of(testCategory.getId()),  // категория
                now.plusDays(4),  // начало диапазона
                now.plusDays(11), // конец диапазона
                false,      // не только доступные
                0, 10,      // пагинация
                true        // сортировка по дате
        );

        // Проверка
        assertThat(events).hasSize(2); // Концерт 1 и Концерт 2
        assertThat(events.stream().map(Event::getTitle))
                .containsExactlyInAnyOrder("Концерт 1", "Концерт 2");
    }

    // ==================== Интеграционные тесты ====================
    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл события")
    void integrationTest_EventLifecycle() {
        // 1. Создание события
        Event created = eventDao.create(testNewEvent);
        assertThat(created.getId()).isNotNull();

        // 2. Поиск по ID
        Event found = eventDao.findById(created.getId());
        assertThat(found.getTitle()).isEqualTo("Рок-концерт");

        // 3. Поиск по пользователю
        List<Event> userEvents = eventDao.findByUser(testUser, 0, 10);
        assertThat(userEvents).hasSize(1);

        // 4. Обновление события
        found.setTitle("Обновленный рок-концерт");
        found.setState(EventState.PUBLICATION_CANCELED);
        Event updated = eventDao.update(found);
        assertThat(updated.getTitle()).isEqualTo("Обновленный рок-концерт");
        assertThat(updated.getState()).isEqualTo(EventState.PUBLICATION_CANCELED);

        // 5. Поиск с фильтрацией
        List<Event> canceledEvents = eventDao.findByFilter(
                null, List.of(EventState.PUBLICATION_CANCELED), null, null, null, 0, 10
        );
        assertThat(canceledEvents).hasSize(1);
        assertThat(canceledEvents.get(0).getId()).isEqualTo(created.getId());

        // 6. Поиск по тексту
        List<Event> textSearch = eventDao.findByFilter(
                "обновленный", null, null, null, null, null, 0, 10, true
        );
        assertThat(textSearch).hasSize(1);
    }

    @Test
    @DisplayName("Должен обрабатывать все параметры как null")
    void shouldHandleAllNullParameters() {
        // Подготовка
        createMultipleTestEvents();

        // Действие - все параметры null
        List<Event> events = eventDao.findByFilter(
                null, null, null, null, null, 0, 10
        );

        // Проверка - должны вернуться все события
        assertThat(events).hasSize(5);
    }
}
