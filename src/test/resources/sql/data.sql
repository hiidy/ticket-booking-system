use ticket_test;

TRUNCATE TABLE booking;
TRUNCATE TABLE show_seat;
TRUNCATE TABLE seat;
TRUNCATE TABLE `show`;
TRUNCATE TABLE event;
TRUNCATE TABLE venue;

INSERT INTO venue (id, name, total_seats)
VALUES (1, '테스트공연장', 100);

INSERT INTO event (id, title, description, type)
VALUES (1, '테스트공연', '테스트공연', 'MUSICAL');

INSERT INTO `show` (id, event_id, date, start_time, end_time)
VALUES (1, 1, '2025-01-01', '18:00:00', '20:00:00');

INSERT INTO seat (id, grade, venue_id, seat_number)
VALUES (1, 'VIP', 1, 1),
       (2, 'VIP', 1, 2),
       (3, 'VIP', 1, 3),
       (4, 'VIP', 1, 4),
       (5, 'VIP', 1, 5),
       (6, 'VIP', 1, 6),
       (7, 'VIP', 1, 7),
       (8, 'VIP', 1, 8),
       (9, 'VIP', 1, 9),
       (10, 'VIP', 1, 10);

INSERT INTO show_seat (id, show_id, seat_id, price, version, status)
VALUES (1, 1, 1, 60000, 0, 'AVAILABLE'),
       (2, 1, 2, 60000, 0, 'AVAILABLE'),
       (3, 1, 3, 60000, 0, 'AVAILABLE'),
       (4, 1, 4, 60000, 0, 'AVAILABLE'),
       (5, 1, 5, 60000, 0, 'AVAILABLE'),
       (6, 1, 6, 60000, 0, 'AVAILABLE'),
       (7, 1, 7, 60000, 0, 'AVAILABLE'),
       (8, 1, 8, 60000, 0, 'AVAILABLE'),
       (9, 1, 9, 60000, 0, 'AVAILABLE'),
       (10, 1, 10, 60000, 0, 'AVAILABLE');

INSERT INTO member (id, name, email, password)
VALUES (1, '김철수', 'kim@gmail.com', '1234'),
       (2, '이영희', 'lee@gmail.com', '1234'),
       (3, '박지민', 'park@gmail.com', '1234'),
       (4, '정다희', 'jung@gmail.com', '1234'),
       (5, '강민수', 'kang@gmail.com', '1234'),
       (6, '윤서연', 'yoon@gmail.com', '1234'),
       (7, '임재현', 'lim@gmail.com', '1234'),
       (8, '한소희', 'han@gmail.com', '1234'),
       (9, '조민준', 'cho@gmail.com', '1234'),
       (10, '송지원', 'song@gmail.com', '1234');
