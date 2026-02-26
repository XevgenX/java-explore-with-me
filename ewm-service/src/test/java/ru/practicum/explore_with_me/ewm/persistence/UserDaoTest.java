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
import ru.practicum.explore_with_me.ewm.domain.model.user.NewUser;
import ru.practicum.explore_with_me.ewm.domain.model.user.User;
import ru.practicum.explore_with_me.ewm.persistence.mapper.UserPersistenceMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
@Import({UserDao.class,  UserPersistenceMapper.class})
@EnableJpaRepositories(basePackages = "ru.practicum.explore_with_me.ewm.persistence.repository")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@DisplayName("Тесты DAO для пользователей")
public class UserDaoTest {

    @Autowired
    private UserDao userDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM users");

        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
    }

    private void insertTestUser(String name, String email) {
        jdbcTemplate.update(
                "INSERT INTO users (name, email) VALUES (?, ?)",
                name, email
        );
    }

    private void insertTestUsers() {
        insertTestUser("Иван Петров", "ivan@example.com");
        insertTestUser("Мария Сидорова", "maria@example.com");
        insertTestUser("Петр Иванов", "petr@example.com");
    }

    // ==================== Тесты findByIds ====================
    @Test
    @DisplayName("findByIds - должен возвращать пользователей по списку ID с пагинацией")
    void findByIds_ShouldReturnUsersByIds() {
        // Подготовка
        insertTestUsers(); // добавляем 3 пользователей

        // Действие
        List<User> users = userDao.findByIds(List.of(1L, 2L), 0, 10);

        // Проверка
        assertAll(
                () -> assertThat(users).hasSize(2),
                () -> assertThat(users.get(0).getId()).isEqualTo(1L),
                () -> assertThat(users.get(0).getName()).isEqualTo("Иван Петров"),
                () -> assertThat(users.get(0).getEmail()).isEqualTo("ivan@example.com"),
                () -> assertThat(users.get(1).getId()).isEqualTo(2L),
                () -> assertThat(users.get(1).getName()).isEqualTo("Мария Сидорова"),
                () -> assertThat(users.get(1).getEmail()).isEqualTo("maria@example.com")
        );
    }

    @Test
    @DisplayName("findByIds - должен возвращать всех пользователей при ids = null")
    void findByIds_ShouldReturnAllUsersWhenIdsNull() {
        // Подготовка
        insertTestUsers(); // добавляем 3 пользователей

        // Действие
        List<User> users = userDao.findByIds(null, 0, 10);

        // Проверка
        assertAll(
                () -> assertThat(users).hasSize(3),
                () -> assertThat(users.get(0).getName()).isEqualTo("Иван Петров"),
                () -> assertThat(users.get(1).getName()).isEqualTo("Мария Сидорова"),
                () -> assertThat(users.get(2).getName()).isEqualTo("Петр Иванов")
        );
    }

    @Test
    @DisplayName("findByIds - должен учитывать пагинацию")
    void findByIds_ShouldRespectPagination() {
        // Подготовка
        insertTestUsers(); // добавляем 3 пользователей

        // Действие - первые 2 пользователя
        List<User> firstPage = userDao.findByIds(null, 0, 2);

        // Проверка
        assertAll(
                () -> assertThat(firstPage).hasSize(2),
                () -> assertThat(firstPage.get(0).getId()).isEqualTo(1L),
                () -> assertThat(firstPage.get(1).getId()).isEqualTo(2L)
        );

        // Действие - вторая страница (со смещением)
        List<User> secondPage = userDao.findByIds(null, 2, 2);

        // Проверка
        assertAll(
                () -> assertThat(secondPage).hasSize(1),
                () -> assertThat(secondPage.get(0).getId()).isEqualTo(3L)
        );
    }

    @Test
    @DisplayName("findByIds - должен возвращать пустой список при несуществующих ID")
    void findByIds_ShouldReturnEmptyListWhenIdsNotFound() {
        // Подготовка
        insertTestUsers();

        // Действие
        List<User> users = userDao.findByIds(List.of(999L, 1000L), 0, 10);

        // Проверка
        assertThat(users).isEmpty();
    }

    @Test
    @DisplayName("findByIds - должен возвращать пустой список при from больше размера")
    void findByIds_ShouldReturnEmptyListWhenFromExceedsSize() {
        // Подготовка
        insertTestUsers();

        // Действие
        List<User> users = userDao.findByIds(null, 10, 5);

        // Проверка
        assertThat(users).isEmpty();
    }

    // ==================== Тесты findById ====================
    @Test
    @DisplayName("findById - должен возвращать пользователя по ID")
    void findById_ShouldReturnUser() {
        // Подготовка
        insertTestUsers();

        // Действие
        User user = userDao.findById(1L);

        // Проверка
        assertAll(
                () -> assertThat(user.getId()).isEqualTo(1L),
                () -> assertThat(user.getName()).isEqualTo("Иван Петров"),
                () -> assertThat(user.getEmail()).isEqualTo("ivan@example.com")
        );
    }

    @Test
    @DisplayName("findById - должен выбрасывать NotFoundException при несуществующем ID")
    void findById_ShouldThrowNotFoundExceptionWhenUserNotFound() {
        // Подготовка
        insertTestUsers();

        // Действие и проверка
        assertThatThrownBy(() -> userDao.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    // ==================== Тесты create ====================
    @Test
    @DisplayName("create - должен создавать нового пользователя")
    void create_ShouldCreateUser() {
        // Подготовка
        NewUser newUser = NewUser.builder()
                .name("Новый пользователь")
                .email("newuser@example.com")
                .build();

        // Действие
        User created = userDao.create(newUser);

        // Проверка возвращенного объекта
        assertAll(
                () -> assertThat(created.getId()).isNotNull(),
                () -> assertThat(created.getName()).isEqualTo("Новый пользователь"),
                () -> assertThat(created.getEmail()).isEqualTo("newuser@example.com")
        );

        // Проверка, что пользователь действительно сохранен в БД
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE email = ?",
                Integer.class,
                "newuser@example.com"
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("create - должен создавать пользователя с правильными данными в БД")
    void create_ShouldSaveCorrectDataInDatabase() {
        // Подготовка
        NewUser newUser = NewUser.builder()
                .name("Тестовый пользователь")
                .email("test@example.com")
                .build();

        // Действие
        User created = userDao.create(newUser);

        // Проверка данных в БД
        String name = jdbcTemplate.queryForObject(
                "SELECT name FROM users WHERE id = ?",
                String.class,
                created.getId()
        );
        String email = jdbcTemplate.queryForObject(
                "SELECT email FROM users WHERE id = ?",
                String.class,
                created.getId()
        );

        assertAll(
                () -> assertThat(name).isEqualTo("Тестовый пользователь"),
                () -> assertThat(email).isEqualTo("test@example.com")
        );
    }

    @Test
    @DisplayName("create - должен генерировать ID автоматически")
    void create_ShouldGenerateIdAutomatically() {
        // Подготовка
        NewUser user1 = NewUser.builder().name("User1").email("user1@example.com").build();
        NewUser user2 = NewUser.builder().name("User2").email("user2@example.com").build();

        // Действие
        User created1 = userDao.create(user1);
        User created2 = userDao.create(user2);

        // Проверка
        assertAll(
                () -> assertThat(created1.getId()).isEqualTo(1L),
                () -> assertThat(created2.getId()).isEqualTo(2L)
        );
    }

    // ==================== Тесты delete ====================
    @Test
    @DisplayName("delete - должен удалять существующего пользователя")
    void delete_ShouldDeleteExistingUser() {
        // Подготовка
        insertTestUsers();

        // Действие
        userDao.delete(1L);

        // Проверка
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE id = 1",
                Integer.class
        );
        assertThat(count).isEqualTo(0);

        // Проверка, что остальные пользователи не удалились
        Integer totalCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users",
                Integer.class
        );
        assertThat(totalCount).isEqualTo(2);
    }

    @Test
    @DisplayName("delete - не должен выбрасывать исключение при удалении несуществующего пользователя")
    void delete_ShouldNotThrowExceptionWhenDeletingNonExistingUser() {
        // Подготовка
        insertTestUsers();

        // Действие (не должно быть исключения)
        userDao.delete(999L);

        // Проверка, что количество пользователей не изменилось
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users",
                Integer.class
        );
        assertThat(count).isEqualTo(3);
    }

    // ==================== Интеграционные тесты ====================
    @Test
    @DisplayName("Интеграционный тест: создание, поиск и удаление")
    void integrationTest_CreateFindAndDelete() {
        // 1. Создание пользователя
        NewUser newUser = NewUser.builder()
                .name("Интеграционный тест")
                .email("integration@test.com")
                .build();

        User created = userDao.create(newUser);
        assertThat(created.getId()).isNotNull();

        // 2. Поиск по ID
        User found = userDao.findById(created.getId());
        assertAll(
                () -> assertThat(found.getName()).isEqualTo("Интеграционный тест"),
                () -> assertThat(found.getEmail()).isEqualTo("integration@test.com")
        );

        // 3. Поиск в списке
        List<User> users = userDao.findByIds(List.of(created.getId()), 0, 10);
        assertThat(users).hasSize(1);

        // 4. Удаление
        userDao.delete(created.getId());

        // 5. Проверка удаления
        List<User> afterDelete = userDao.findByIds(List.of(created.getId()), 0, 10);
        assertThat(afterDelete).isEmpty();

        // 6. Проверка, что при прямом поиске выбрасывается исключение
        assertThatThrownBy(() -> userDao.findById(created.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("findByIds - должен правильно рассчитывать from для пагинации")
    void findByIds_ShouldCorrectlyCalculateFrom() {
        // Подготовка
        for (int i = 1; i <= 10; i++) {
            insertTestUser("User" + i, "user" + i + "@example.com");
        }

        // Действие: from=5, size=3 означает 5/3=1 страница (пропускаем первых 3)
        List<User> users = userDao.findByIds(null, 5, 3);

        // Проверка: должны быть пользователи с 4 по 6
        assertAll(
                () -> assertThat(users).hasSize(3),
                () -> assertThat(users.get(0).getId()).isEqualTo(4L),
                () -> assertThat(users.get(1).getId()).isEqualTo(5L),
                () -> assertThat(users.get(2).getId()).isEqualTo(6L)
        );
    }
}
