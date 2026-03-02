package ru.practicum.explore_with_me.ewm.persistence;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.explore_with_me.common.domain.exception.NotFoundException;
import ru.practicum.explore_with_me.ewm.domain.model.category.Category;
import ru.practicum.explore_with_me.ewm.domain.model.comment.Comment;
import ru.practicum.explore_with_me.ewm.domain.model.comment.CommentStatus;
import ru.practicum.explore_with_me.ewm.domain.model.comment.NewComment;
import ru.practicum.explore_with_me.ewm.domain.model.event.Event;
import ru.practicum.explore_with_me.ewm.domain.model.event.EventState;
import ru.practicum.explore_with_me.ewm.domain.model.event.Location;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.persistence.mapper.CommentPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.mapper.EventPersistenceMapper;
import ru.practicum.explore_with_me.ewm.persistence.mapper.UserPersistenceMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({CommentDao.class, CommentPersistenceMapper.class, EventPersistenceMapper.class, UserPersistenceMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для комментариев")
public class CommentDaoTest {

    @Autowired
    private CommentDao commentDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private User testAuthor;
    private User testInitiator;
    private Event testEvent;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        // Отключаем проверку внешних ключей для очистки
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        jdbcTemplate.execute("DELETE FROM comments");
        jdbcTemplate.execute("DELETE FROM compilation_events");
        jdbcTemplate.execute("DELETE FROM compilations");
        jdbcTemplate.execute("DELETE FROM participation_requests");
        jdbcTemplate.execute("DELETE FROM events");
        jdbcTemplate.execute("DELETE FROM locations");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("DELETE FROM category");

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        // Сброс автоинкремента для всех таблиц
        jdbcTemplate.execute("ALTER TABLE comments ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE events ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE category ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE locations ALTER COLUMN id RESTART WITH 1");

        now = LocalDateTime.now();

        insertTestCategory("Тестовая категория");
        insertTestUser("Автор Комментария", "author@test.com");
        insertTestUser("Инициатор События", "initiator@test.com");
        insertTestLocation(55.754f, 37.62f);
        insertTestEvent(2L, 1L, 1L); // event с initiatorId=2, categoryId=1, locationId=1

        testAuthor = User.builder()
                .id(1L)
                .name("Автор Комментария")
                .email("author@test.com")
                .build();

        testInitiator = User.builder()
                .id(2L)
                .name("Инициатор События")
                .email("initiator@test.com")
                .build();
        var testCategory = Category.builder()
                .id(1L)
                .name("Концерты")
                .build();

        var testLocation = Location.builder()
                .lat(55.754167F)
                .lon(37.62F)
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
    }

    private void insertTestCategory(String name) {
        jdbcTemplate.update(
                "INSERT INTO category (name) VALUES (?)",
                name
        );
    }

    private void insertTestUser(String name, String email) {
        jdbcTemplate.update(
                "INSERT INTO users (name, email) VALUES (?, ?)",
                name, email
        );
    }

    private void insertTestLocation(float lat, float lon) {
        jdbcTemplate.update(
                "INSERT INTO locations (lat, lon) VALUES (?, ?)",
                lat, lon
        );
    }

    private void insertTestEvent(Long initiatorId, Long categoryId, Long locationId) {
        jdbcTemplate.update(
                "INSERT INTO events (title, annotation, description, event_date, location_id, " +
                        "paid, participant_limit, request_moderation, confirmed_requests, created_on, initiator_id, category_id, state) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                "Тестовое событие", "Тестовая аннотация", "Тестовое описание",
                LocalDateTime.now().plusDays(1), locationId,
                false, 10, true, 0, LocalDateTime.now(),
                initiatorId, categoryId, EventState.PUBLISHED.name()
        );
    }

    private void insertTestComment(Long authorId, Long eventId, CommentStatus status) {
        insertTestComment(authorId, eventId, status, "Тестовый комментарий");
    }

    private void insertTestComment(Long authorId, Long eventId, CommentStatus status, String text) {
        jdbcTemplate.update(
                "INSERT INTO comments (text, author_id, event_id, created, status) VALUES (?, ?, ?, ?, ?)",
                text, authorId, eventId, now, status.name()
        );
    }

    private void insertTestCommentWithUpdate(Long authorId, Long eventId, CommentStatus status,
                                             LocalDateTime created, LocalDateTime updated) {
        jdbcTemplate.update(
                "INSERT INTO comments (text, author_id, event_id, created, updated, status) VALUES (?, ?, ?, ?, ?, ?)",
                "Комментарий с обновлением", authorId, eventId, created, updated, status.name()
        );
    }

    private void insertTestComments() {
        insertTestComment(1L, 1L, CommentStatus.PENDING);
        insertTestComment(1L, 1L, CommentStatus.APPROVED);
        insertTestComment(1L, 1L, CommentStatus.REJECTED);
        insertTestComment(2L, 1L, CommentStatus.PENDING);
    }

    @Test
    @DisplayName("findAllByEvent - должен возвращать пустой список, если у события нет комментариев")
    void findAllByEvent_ShouldReturnEmptyListWhenNoComments() {
        List<Comment> comments = commentDao.findAllByEvent(testEvent);

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("findApprovedEvent - должен возвращать пустой список, если нет одобренных комментариев")
    void findApprovedEvent_ShouldReturnEmptyListWhenNoApprovedComments() {
        insertTestComment(1L, 1L, CommentStatus.PENDING);
        insertTestComment(1L, 1L, CommentStatus.REJECTED);

        List<Comment> comments = commentDao.findApprovedEvent(testEvent);

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("findByUser - должен возвращать пустой список, если у пользователя нет комментариев")
    void findByUser_ShouldReturnEmptyListWhenUserHasNoComments() {
        User userWithNoComments = User.builder()
                .id(999L)
                .name("Нет комментариев")
                .email("nocomments@test.com")
                .build();

        List<Comment> comments = commentDao.findByUser(userWithNoComments);

        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException для несуществующего ID")
    void findById_ShouldThrowNotFoundExceptionWhenNotExists() {
        assertThrows(NotFoundException.class, () -> commentDao.findById(999L));
    }

    @Test
    @DisplayName("update - должен выбрасывать исключение при обновлении несуществующего комментария")
    void update_ShouldThrowExceptionWhenCommentNotExists() {
        Comment nonExistentComment = Comment.builder()
                .id(999L)
                .text("Несуществующий комментарий")
                .event(testEvent)
                .author(testAuthor)
                .createdOn(now)
                .status(CommentStatus.PENDING)
                .build();

        assertThrows(Exception.class, () -> commentDao.update(nonExistentComment));
    }

    @Test
    @DisplayName("delete - не должен выбрасывать исключение при удалении несуществующего комментария")
    void delete_ShouldNotThrowExceptionWhenDeletingNonExistent() {
        assertDoesNotThrow(() -> commentDao.delete(999L));
    }

    @Test
    @DisplayName("create - должен корректно сохранять комментарий с максимальной длиной текста")
    void create_ShouldSaveCommentWithMaxLengthText() {
        String longText = "A".repeat(250); // MAX 250 символов согласно DDL

        NewComment newComment = new NewComment(
                longText,
                testEvent,
                testAuthor,
                now
        );

        Comment created = commentDao.create(newComment);

        assertAll(
                () -> assertThat(created.getId()).isEqualTo(1L),
                () -> assertThat(created.getText()).hasSize(250),
                () -> assertThat(created.getText()).isEqualTo(longText)
        );
    }
}