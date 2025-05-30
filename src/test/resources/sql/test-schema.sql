DROP DATABASE ticket_test;
CREATE DATABASE ticket_test;
USE ticket_test;

CREATE TABLE seat
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    seat_number INT         NOT NULL,
    grade       VARCHAR(50) NOT NULL,
    venue_id    INT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE member
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50)  NOT NULL,
    email      VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE `show`
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE show_time
(
    id         INT AUTO_INCREMENT PRIMARY KEY,
    show_id   INT,
    date       DATE      NOT NULL,
    start_time TIME      NOT NULL,
    end_time   TIME      NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE booking
(
    id           INT AUTO_INCREMENT PRIMARY KEY,
    member_id    INT       NOT NULL,
    total_amount BIGINT    NOT NULL,
    timestamp    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE show_seat
(
    id              INT AUTO_INCREMENT PRIMARY KEY,
    show_time_id         INT          NOT NULL,
    seat_id         INT          NOT NULL,
    booking_id      INT,
    price           BIGINT       NOT NULL,
    status          VARCHAR(100) NOT NULL,
    expiration_time TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE venue
(
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    total_seats INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE inventory
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id         BIGINT      NOT NULL,
    grade           VARCHAR(50) NOT NULL,
    total_count     INT         NOT NULL,
    available_count INT         NOT NULL
);

CREATE INDEX idx_show_seat_show_seat_id ON show_seat (show_id, seat_id, status);
CREATE INDEX idx_show_date_show_id ON showTime (date, show_id);
CREATE INDEX idx_inventory_show_id_grade ON inventory (show_id, grade);
