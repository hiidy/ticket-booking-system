DROP DATABASE IF EXISTS ticket_booking;
CREATE DATABASE ticket_booking;
USE ticket_booking;

CREATE TABLE venue
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100),
    total_seats INT       NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

CREATE TABLE seat
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    seat_number   INT         NOT NULL,
    section_id    VARCHAR(20) NOT NULL,
    section_group VARCHAR(10) NOT NULL,
    grade         ENUM('VIP', 'R', 'S', 'A') NOT NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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

CREATE TABLE booking
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id   BINARY(16) UNIQUE,
    member_id    BIGINT      NOT NULL,
    total_amount INT         NOT NULL,
    status       ENUM('PENDING', 'FAILED', 'SUCCESS') NOT NULL,
    timestamp    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE ticket
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_time_id    BIGINT       NOT NULL,
    seat_id         BIGINT       NOT NULL,
    booking_id      BIGINT,
    price           INT          NOT NULL,
    status          ENUM('AVAILABLE', 'PAYMENT_PENDING', 'BOOKED', 'CANCELLED') NOT NULL,
    expiration_time TIMESTAMP,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE inventory
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    show_id         BIGINT      NOT NULL,
    grade           VARCHAR(50) NOT NULL,
    total_count     INT         NOT NULL,
    available_count INT         NOT NULL
);

CREATE TABLE stream_message
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_name VARCHAR(50) NOT NULL,
    message_id  VARCHAR(50) NOT NULL,
    status      VARCHAR(50) NOT NULL
);

-- 인덱스 생성
CREATE INDEX ticket_show_time_id_seat_id_status_index ON ticket (show_time_id, seat_id, status);
CREATE INDEX idx_show_date_show_id ON show_time (date, show_id);
CREATE INDEX idx_inventory_show_id_grade ON inventory (show_id, grade);
CREATE INDEX idx_booking_request_id ON booking (request_id);
CREATE INDEX idx_ticket_booking_id ON ticket (booking_id);

SELECT '테이블 스키마 생성 완료!' as message;