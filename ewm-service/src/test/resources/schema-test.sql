-- Отключаем проверку внешних ключей для H2
SET REFERENTIAL_INTEGRITY FALSE;

-- Таблица users
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE
);

-- Таблица category (обратите внимание на имя - оно должно совпадать с Entity)
CREATE TABLE IF NOT EXISTS category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    CONSTRAINT uq_category_name UNIQUE (name)
);

-- Таблица locations
CREATE TABLE IF NOT EXISTS locations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lat FLOAT NOT NULL,
    lon FLOAT NOT NULL
);

-- Таблица events
CREATE TABLE IF NOT EXISTS events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    initiator_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    created_on TIMESTAMP NOT NULL,
    title VARCHAR(120) NOT NULL,
    annotation VARCHAR(2000) NOT NULL,
    description VARCHAR(7000) NOT NULL,
    event_date TIMESTAMP NOT NULL,
    paid BOOLEAN NOT NULL,
    participant_limit INTEGER NOT NULL,
    request_moderation BOOLEAN NOT NULL,
    confirmed_requests INTEGER NOT NULL DEFAULT 0,
    published_on TIMESTAMP,
    state VARCHAR(50) NOT NULL,
    CONSTRAINT fk_events_initiator FOREIGN KEY (initiator_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_events_category FOREIGN KEY (category_id)
        REFERENCES category(id) ON DELETE RESTRICT,
    CONSTRAINT fk_events_location FOREIGN KEY (location_id)
        REFERENCES locations(id) ON DELETE CASCADE
);

-- Таблица participation_requests
CREATE TABLE IF NOT EXISTS participation_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    requester_id BIGINT NOT NULL,
    created TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    CONSTRAINT fk_requests_event FOREIGN KEY (event_id)
        REFERENCES events(id) ON DELETE CASCADE,
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id)
        REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT uq_requester_event UNIQUE (requester_id, event_id)
);

-- Таблица compilations
CREATE TABLE IF NOT EXISTS compilations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(50) NOT NULL,
    pinned BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_compilation_title UNIQUE (title)
);

-- Таблица compilation_events
CREATE TABLE IF NOT EXISTS compilation_events (
    compilation_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    PRIMARY KEY (compilation_id, event_id),
    FOREIGN KEY (compilation_id) REFERENCES compilations(id) ON DELETE CASCADE,
    FOREIGN KEY (event_id) REFERENCES events(id) ON DELETE CASCADE
);

-- Включаем проверку внешних ключей обратно
SET REFERENTIAL_INTEGRITY TRUE;