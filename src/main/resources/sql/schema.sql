DROP DATABASE IF EXISTS booking_system;
CREATE DATABASE booking_system;
USE booking_system;

CREATE TABLE seat
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number INT         NOT NULL,
    grade       VARCHAR(50) NOT NULL,
    venue_id    BIGINT      NOT NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE member
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `show`
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE show_time
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id    BIGINT    NOT NULL,
    venue_id   BIGINT    NOT NULL,
    date       DATE      NOT NULL,
    start_time TIME      NOT NULL,
    end_time   TIME      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE booking
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id   BINARY(16) UNIQUE,
    member_id    BIGINT    NOT NULL,
    total_amount BIGINT    NOT NULL,
    timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE ticket
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_time_id    BIGINT       NOT NULL,
    seat_id         BIGINT       NOT NULL,
    booking_id      BIGINT,
    price           BIGINT       NOT NULL,
    status          VARCHAR(100) NOT NULL,
    expiration_time TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE venue
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    total_seats INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE inventory
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_time_id    BIGINT      NOT NULL,
    grade           VARCHAR(50) NOT NULL,
    total_count     INT         NOT NULL,
    available_count INT         NOT NULL
);

create table stream_message
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_name VARCHAR(50) NOT NULL,
    message_id  VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL
);

CREATE INDEX show_type_index ON `show` (type);
CREATE INDEX ticket_show_time_id_seat_id_status_index ON ticket (show_time_id, seat_id, status);
CREATE INDEX idx_show_date_show_id ON show_time (date, show_id);
CREATE INDEX idx_inventory_show_id_grade ON inventory (show_id, grade);
