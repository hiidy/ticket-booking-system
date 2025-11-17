-- Load Test용 초기 데이터 생성 스크립트
-- 인기 공연 하나에 대한 45000개 좌석과 100만명 회원 데이터 생성
-- 데이터가 이미 존재하면 생성하지 않음

-- 문자셋 설정
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;

USE ticket_booking;

-- 데이터가 이미 있는지 확인
SELECT COUNT(*) as venue_count
FROM venue
WHERE id = 1;

-- 공연장 데이터가 없을 경우에만 생성 (INSERT IGNORE 사용)
SELECT 'Load Test 데이터 생성 시작...' as message;

-- 1. 공연장 정보 생성 (단일 공연장)
INSERT IGNORE INTO venue (id, name, total_seats, created_at, updated_at)
VALUES (1, '올림픽 파크 아레나', 45000, NOW(), NOW());

-- 2. 인기 공연 정보 생성 (단일 공연)
INSERT IGNORE INTO `show` (id, title, description, type, created_at, updated_at)
VALUES (1, '슈퍼스타 K 페스티벌 2025', '한국 최고의 K-POP 아티스트들이 함께하는 초대형 페스티벌', 'CONCERT', NOW(), NOW());

-- 3. 공연 시간 정보 생성 (인기 있는 공연 시간)
INSERT IGNORE INTO show_time (id, show_id, venue_id, date, start_time, end_time, created_at,
                              updated_at)
VALUES (1, 1, 1, CURDATE() + INTERVAL 30 DAY, '19:00:00', '23:00:00', NOW(), NOW());

-- 4. 100만명 회원 데이터 생성 (재귀 CTE 사용)
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

-- 5. 45000개 좌석 생성 (간단한 방식)
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
INSERT IGNORE INTO ticket (id, show_time_id, seat_id, price, status, created_at, updated_at)
SELECT s.id,
       1,    -- show_time_id
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

-- 7. 데이터 생성 확인
SELECT '데이터 생성 확인' as info, '' as details
UNION ALL
SELECT '공연장 정보', CONCAT('총 ', COUNT(*), '개')
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
SELECT '공연 시간 정보', CONCAT('총 ', COUNT(*), '개')
FROM show_time
UNION ALL
SELECT '티켓 정보',
       CONCAT('총 ', COUNT(*), '매 (AVAILABLE: ',
              SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END), ')')
FROM ticket;

-- 8. 좌석 그룹별 현황 확인
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

-- 9. 인기 섹션 티켓 확인 (VIP 및 전방 좌석)
SELECT s.section_group,
       s.grade,
       s.section_id,
       COUNT(*)     as available_tickets,
       MIN(t.price) as min_price,
       MAX(t.price) as max_price
FROM ticket t
         JOIN seat s ON t.seat_id = s.id
WHERE t.status = 'AVAILABLE'
  AND t.show_time_id = 1
  AND s.section_group IN ('G1', 'G2', 'G3', 'P', 'R')
GROUP BY s.section_group, s.section_id, s.grade
ORDER BY FIELD(s.section_group, 'G1', 'G2', 'G3', 'P', 'R'), s.section_id
LIMIT 20;

SELECT 'Load Test 데이터 생성 완료!' as message;