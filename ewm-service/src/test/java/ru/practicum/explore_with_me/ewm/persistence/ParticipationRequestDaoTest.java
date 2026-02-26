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
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewLocation;
import ru.practicum.explore_with_me.ewm.domain.model.event.NewEvent;
import ru.practicum.explore_with_me.ewm.domain.model.request.NewParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.ParticipationRequest;
import ru.practicum.explore_with_me.ewm.domain.model.request.RequestStatus;
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
        ParticipationRequestDao.class,
        ParticipationRequestPersistenceMapper.class,
        EventDao.class,
        EventPersistenceMapper.class,
        UserDao.class,
        UserPersistenceMapper.class,
        CategoryDao.class,
        CategoryPersistenceMapper.class
})
@EnableJpaRepositories(basePackages = "ru.practicum.explore_with_me.ewm.persistence.repository")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для запросов на участие")
public class ParticipationRequestDaoTest {

    @Autowired
    private ParticipationRequestDao requestDao;

    @Autowired
    private EventDao eventDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private CategoryDao categoryDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User requester;
    private User initiator;
    private Category category;
    private Location location;
    private NewLocation newLocation;
    private Event event;
    private NewParticipationRequest newRequest;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы в правильном порядке
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
        jdbcTemplate.execute("ALTER TABLE participation_requests ALTER COLUMN id RESTART WITH 1");

        // Создаем пользователей
        requester = userDao.create(NewUser.builder()
                .name("Заявитель")
                .email("requester@example.com")
                .build());

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

        // Создаем событие
        NewEvent newEvent = NewEvent.builder()
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

        event = eventDao.create(newEvent);

        // Создаем новый запрос
        newRequest = NewParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
    }

    private ParticipationRequest createTestRequest(RequestStatus status) {
        NewParticipationRequest request = NewParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(status)
                .created(LocalDateTime.now())
                .build();
        return requestDao.create(request);
    }

    private List<ParticipationRequest> createMultipleRequests() {
        ParticipationRequest req1 = createTestRequest(RequestStatus.PENDING);

        // Создаем второго заявителя
        User anotherRequester = userDao.create(NewUser.builder()
                .name("Другой заявитель")
                .email("another@example.com")
                .build());

        NewParticipationRequest request2 = NewParticipationRequest.builder()
                .event(event)
                .requester(anotherRequester)
                .status(RequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();
        ParticipationRequest req2 = requestDao.create(request2);

        NewParticipationRequest request3 = NewParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.REJECTED)
                .created(LocalDateTime.now())
                .build();
        ParticipationRequest req3 = requestDao.create(request3);

        return List.of(req1, req2, req3);
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException при несуществующем ID")
    void findById_ShouldThrowNotFoundExceptionWhenRequestNotFound() {
        assertThatThrownBy(() -> requestDao.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Запрос не найден");
    }

    @Test
    @DisplayName("findByUser - должен возвращать пустой список если у пользователя нет запросов")
    void findByUser_ShouldReturnEmptyListWhenNoRequests() {
        // Подготовка
        User newUser = userDao.create(NewUser.builder()
                .name("Новый пользователь")
                .email("new@example.com")
                .build());

        // Действие
        List<ParticipationRequest> requests = requestDao.findByUser(newUser);

        // Проверка
        assertThat(requests).isEmpty();
    }

    @Test
    @DisplayName("findByEvent - должен возвращать пустой список если на событие нет запросов")
    void findByEvent_ShouldReturnEmptyListWhenNoRequests() {
        // Подготовка
        // Создаем другое событие без запросов
        NewEvent anotherEvent = NewEvent.builder()
                .title("Другое событие")
                .annotation("Аннотация")
                .description("Описание")
                .initiator(initiator)
                .category(category)
                .location(newLocation)
                .eventDate(LocalDateTime.now().plusDays(20))
                .paid(true)
                .participantLimit(50)
                .requestModeration(true)
                .build();
        Event newEvent = eventDao.create(anotherEvent);

        // Действие
        List<ParticipationRequest> requests = requestDao.findByEvent(newEvent);

        // Проверка
        assertThat(requests).isEmpty();
    }

    @Test
    @DisplayName("create - не должен создавать дубликат запроса (unique constraint)")
    void create_ShouldNotCreateDuplicateRequest() {
        // Подготовка
        requestDao.create(newRequest);

        // Действие и проверка
        assertThatThrownBy(() -> requestDao.create(newRequest))
                .isInstanceOf(Exception.class); // DataIntegrityViolationException
    }

    // ==================== Тесты update ====================
    @Test
    @DisplayName("update - должен обновлять существующий запрос")
    void update_ShouldUpdateRequest() {
        // Подготовка
        ParticipationRequest created = requestDao.create(newRequest);
        created.setStatus(RequestStatus.CONFIRMED);

        // Действие
        ParticipationRequest updated = requestDao.update(created);

        // Проверка
        assertAll(
                () -> assertThat(updated.getId()).isEqualTo(created.getId()),
                () -> assertThat(updated.getStatus()).isEqualTo(RequestStatus.CONFIRMED)
        );

        // Проверка в БД
        String statusInDb = jdbcTemplate.queryForObject(
                "SELECT status FROM participation_requests WHERE id = ?",
                String.class,
                created.getId()
        );
        assertThat(statusInDb).isEqualTo("CONFIRMED");
    }

    @Test
    @DisplayName("Интеграционный тест: полный жизненный цикл запроса")
    void integrationTest_RequestLifecycle() {
        // 1. Создание запроса
        ParticipationRequest created = requestDao.create(newRequest);
        assertThat(created.getId()).isNotNull();

        // 2. Поиск по ID
        ParticipationRequest found = requestDao.findById(created.getId());
        assertThat(found.getStatus()).isEqualTo(RequestStatus.PENDING);

        // 3. Поиск по пользователю
        List<ParticipationRequest> userRequests = requestDao.findByUser(requester);
        assertThat(userRequests).hasSize(1);

        // 4. Поиск по событию
        List<ParticipationRequest> eventRequests = requestDao.findByEvent(event);
        assertThat(eventRequests).hasSize(1);

        // 5. Обновление статуса
        found.setStatus(RequestStatus.CONFIRMED);
        ParticipationRequest updated = requestDao.update(found);
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.CONFIRMED);

        // 6. Проверка обновления в списках
        List<ParticipationRequest> updatedEventRequests = requestDao.findByEvent(event);
        assertThat(updatedEventRequests.get(0).getStatus()).isEqualTo(RequestStatus.CONFIRMED);
    }

    @Test
    @DisplayName("Интеграционный тест: несколько запросов на одно событие")
    void integrationTest_MultipleRequests() {
        // Создаем второго заявителя
        User anotherRequester = userDao.create(NewUser.builder()
                .name("Другой заявитель")
                .email("another@example.com")
                .build());

        // Создаем запросы с разными статусами
        NewParticipationRequest request1 = NewParticipationRequest.builder()
                .event(event)
                .requester(requester)
                .status(RequestStatus.PENDING)
                .created(LocalDateTime.now())
                .build();
        ParticipationRequest req1 = requestDao.create(request1);

        NewParticipationRequest request2 = NewParticipationRequest.builder()
                .event(event)
                .requester(anotherRequester)
                .status(RequestStatus.CONFIRMED)
                .created(LocalDateTime.now())
                .build();
        ParticipationRequest req2 = requestDao.create(request2);

        // Проверяем запросы по событию
        List<ParticipationRequest> eventRequests = requestDao.findByEvent(event);
        assertThat(eventRequests).hasSize(2);

        // Проверяем запросы по пользователям
        List<ParticipationRequest> requesterRequests = requestDao.findByUser(requester);
        assertThat(requesterRequests).hasSize(1);
        assertThat(requesterRequests.get(0).getStatus()).isEqualTo(RequestStatus.PENDING);

        List<ParticipationRequest> anotherRequests = requestDao.findByUser(anotherRequester);
        assertThat(anotherRequests).hasSize(1);
        assertThat(anotherRequests.get(0).getStatus()).isEqualTo(RequestStatus.CONFIRMED);

        // Обновляем статус первого запроса
        req1.setStatus(RequestStatus.REJECTED);
        requestDao.update(req1);

        // Проверяем обновление
        ParticipationRequest updated = requestDao.findById(req1.getId());
        assertThat(updated.getStatus()).isEqualTo(RequestStatus.REJECTED);
    }
}
