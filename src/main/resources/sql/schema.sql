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

CREATE TABLE event
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `show`
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id   BIGINT    NOT NULL,
    date       DATE      NOT NULL,
    start_time TIME      NOT NULL,
    end_time   TIME      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE booking
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id    BIGINT    NOT NULL,
    total_amount BIGINT    NOT NULL,
    timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE show_seat
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id         BIGINT       NOT NULL,
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
    show_id         BIGINT      NOT NULL,
    grade           VARCHAR(50) NOT NULL,
    total_count     INT         NOT NULL,
    available_count INT         NOT NULL,
    PRIMARY KEY (show_id, grade)
);

CREATE INDEX idx_show_seat_show_seat_id ON show_seat (show_id, seat_id, status);