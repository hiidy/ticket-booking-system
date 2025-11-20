-- Load Test용 초기 데이터 생성 스크립트
-- 인기 공연 하나에 대한 45000개 좌석과 100만명 회원 데이터 생성

-- 문자셋 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;

USE ticket_booking;

-- 1. 공연장 생성
INSERT IGNORE INTO venue (id, name, total_seats, created_at, updated_at)
VALUES (1, '올림픽 파크 아레나', 45000, NOW(), NOW());

-- 2. 인기 공연  생성 (ShowTime과 통합)
INSERT IGNORE INTO `show` (id, title, description, type, venue_id, date, start_time, end_time, created_at, updated_at)
VALUES (1, '슈퍼스타 K 페스티벌 2025', '한국 최고의 K-POP 아티스트들이 함께하는 초대형 페스티벌', 'CONCERT', 1, CURDATE() + INTERVAL 30 DAY, '19:00:00', '23:00:00', NOW(), NOW());

-- 4. 100만명 회원 데이터 생성
SET SESSION cte_max_recursion_depth = 1000000;

INSERT IGNORE INTO member (id, name, email, password, created_at, updated_at)
WITH RECURSIVE numbers(n) AS (SELECT 1
                              UNION ALL
                              SELECT n + 1
                              FROM numbers
                              WHERE n < 1000000)
SELECT n,
       CONCAT('테스트유저', n),
       CONCAT('user', n, '@test.com'),
       '$2a$10$dummy.hashed.password',
       NOW(),
       NOW()
FROM numbers;

-- 5. 45000개 좌석 생성
-- G1: 6400석, G2: 7200석, G3: 6480석, P: 10000석, R: 4950석, S: 3156석, A: 6814석
SET SESSION cte_max_recursion_depth = 100000;

INSERT IGNORE INTO seat (id, seat_number, section_id, section_group, grade, created_at, updated_at)
SELECT n,
       ((n - 1) % 100) + 1, -- 섹션당 좌석 번호 (1-100)
       CEIL(n / 100),       -- 섹션 ID
       CASE
           WHEN n <= 6400 THEN 'G1'
           WHEN n <= 13600 THEN 'G2'
           WHEN n <= 20080 THEN 'G3'
           WHEN n <= 30080 THEN 'P'
           WHEN n <= 35030 THEN 'R'
           WHEN n <= 38186 THEN 'S'
           ELSE 'A'
           END,
       CASE
           WHEN n <= 20080 THEN 'VIP' -- G1, G2, G3는 VIP
           WHEN n <= 35030 THEN 'R' -- P, R는 R
           WHEN n <= 38186 THEN 'S' -- S는 S
           ELSE 'A' -- A는 A
           END,
       NOW(),
       NOW()
FROM (WITH RECURSIVE numbers(n) AS (SELECT 1
                                    UNION ALL
                                    SELECT n + 1
                                    FROM numbers
                                    WHERE n < 45000)
      SELECT n
      FROM numbers) AS seat_numbers;

-- 6. 45000개 티켓 생성 (모든 좌석에 대해 AVAILABLE 상태)
INSERT IGNORE INTO ticket (id, show_id, seat_id, price, status, created_at, updated_at)
SELECT s.id,
       1,    -- show_id
       s.id, -- seat_id
       CASE s.grade
           WHEN 'VIP' THEN 200000
           WHEN 'R' THEN 120000
           WHEN 'S' THEN 100000
           WHEN 'A' THEN 80000
           ELSE 50000
           END,
       'AVAILABLE',
       NOW(),
       NOW()
FROM seat s;

-- 7. 데이터 확인
SELECT 'Load Test 데이터 생성 최종 확인' as info;
SELECT '공연장 정보', CONCAT('총 ', COUNT(*), '개') as count
FROM venue
UNION ALL
SELECT '공연 정보', CONCAT('총 ', COUNT(*), '개')
FROM `show`
UNION ALL
SELECT '회원 정보', CONCAT('총 ', COUNT(*), '명')
FROM member
UNION ALL
SELECT '좌석 정보', CONCAT('총 ', COUNT(*), '석')
FROM seat
UNION ALL
SELECT '티켓 정보',
       CONCAT('총 ', COUNT(*), '매 (AVAILABLE: ',
              SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END), ')')
FROM ticket;

-- 8. 좌석 그룹별 확인
SELECT section_group,
       grade,
       COUNT(*)     as seat_count,
       MIN(t.price) as min_price,
       MAX(t.price) as max_price,
       COUNT(*)     as available_tickets
FROM seat s
         JOIN ticket t ON s.id = t.seat_id
WHERE t.status = 'AVAILABLE'
GROUP BY s.section_group, s.grade
ORDER BY FIELD(s.section_group, 'G1', 'G2', 'G3', 'P', 'R', 'S', 'A');

SELECT 'Load Test 데이터 생성 완료!' as message;