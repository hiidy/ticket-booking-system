-- Load Test용 초기 데이터 생성 스크립트
-- 브루노 마스 콘서트 50,000개 섹션별 좌석과 100만명 회원 데이터 생성

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;

USE ticket_booking;

-- 1. 공연장 생성
INSERT IGNORE INTO venue (id, name, total_seats, created_at, updated_at)
VALUES (1, '잠실 종합운동장', 50000, NOW(), NOW());

-- 2. 공연 생성
INSERT IGNORE INTO `show` (id, title, description, type, venue_id, date, start_time, end_time, created_at, updated_at)
VALUES (1, 'Bruno Mars World Tour 2025', '그래미 상 수상 아티스트 브루노 마스의 월드 투어 한국 공연', 'CONCERT', 1, CURDATE() + INTERVAL 30 DAY, '19:00:00', '23:00:00', NOW(), NOW());

-- 3. 섹션 생성
-- G1: 10개 섹션 x 800석 = 8,000석 (16%)
-- G2: 12개 섹션 x 600석 = 7,200석 (14%)
-- G3: 12개 섹션 x 600석 = 7,200석 (14%)
-- P: 20개 섹션 x 550석 = 11,000석 (22%)
-- R: 11개 섹션 x 600석 = 6,600석 (13%)
-- S: 12개 섹션 x 500석 = 6,000석 (12%)
-- A: 9개 섹션 x 444석 = 4,000석 (8%)
-- 총계: 50,000석

INSERT IGNORE INTO section (id, show_id, section_name, section_type, base_price, total_seats, available_seats, created_at, updated_at)
VALUES
-- G1 그룹 (1~2층 그라운드 중앙, 103~107, 203~207)
(1, 1, 'G1_103', 'G1', 200000, 800, 800, NOW(), NOW()),
(2, 1, 'G1_104', 'G1', 200000, 800, 800, NOW(), NOW()),
(3, 1, 'G1_105', 'G1', 200000, 800, 800, NOW(), NOW()),
(4, 1, 'G1_106', 'G1', 200000, 800, 800, NOW(), NOW()),
(5, 1, 'G1_107', 'G1', 200000, 800, 800, NOW(), NOW()),
(6, 1, 'G1_203', 'G1', 200000, 800, 800, NOW(), NOW()),
(7, 1, 'G1_204', 'G1', 200000, 800, 800, NOW(), NOW()),
(8, 1, 'G1_205', 'G1', 200000, 800, 800, NOW(), NOW()),
(9, 1, 'G1_206', 'G1', 200000, 800, 800, NOW(), NOW()),
(10, 1, 'G1_207', 'G1', 200000, 800, 800, NOW(), NOW()),

-- G2 그룹 (3층 스탠드 전면, 301~312)
(11, 1, 'G2_301', 'G2', 180000, 600, 600, NOW(), NOW()),
(12, 1, 'G2_302', 'G2', 180000, 600, 600, NOW(), NOW()),
(13, 1, 'G2_303', 'G2', 180000, 600, 600, NOW(), NOW()),
(14, 1, 'G2_304', 'G2', 180000, 600, 600, NOW(), NOW()),
(15, 1, 'G2_305', 'G2', 180000, 600, 600, NOW(), NOW()),
(16, 1, 'G2_306', 'G2', 180000, 600, 600, NOW(), NOW()),
(17, 1, 'G2_307', 'G2', 180000, 600, 600, NOW(), NOW()),
(18, 1, 'G2_308', 'G2', 180000, 600, 600, NOW(), NOW()),
(19, 1, 'G2_309', 'G2', 180000, 600, 600, NOW(), NOW()),
(20, 1, 'G2_310', 'G2', 180000, 600, 600, NOW(), NOW()),
(21, 1, 'G2_311', 'G2', 180000, 600, 600, NOW(), NOW()),
(22, 1, 'G2_312', 'G2', 180000, 600, 600, NOW(), NOW()),

-- G3 그룹 (4층 스탠드, 401~412)
(23, 1, 'G3_401', 'G3', 160000, 600, 600, NOW(), NOW()),
(24, 1, 'G3_402', 'G3', 160000, 600, 600, NOW(), NOW()),
(25, 1, 'G3_403', 'G3', 160000, 600, 600, NOW(), NOW()),
(26, 1, 'G3_404', 'G3', 160000, 600, 600, NOW(), NOW()),
(27, 1, 'G3_405', 'G3', 160000, 600, 600, NOW(), NOW()),
(28, 1, 'G3_406', 'G3', 160000, 600, 600, NOW(), NOW()),
(29, 1, 'G3_407', 'G3', 160000, 600, 600, NOW(), NOW()),
(30, 1, 'G3_408', 'G3', 160000, 600, 600, NOW(), NOW()),
(31, 1, 'G3_409', 'G3', 160000, 600, 600, NOW(), NOW()),
(32, 1, 'G3_410', 'G3', 160000, 600, 600, NOW(), NOW()),
(33, 1, 'G3_411', 'G3', 160000, 600, 600, NOW(), NOW()),
(34, 1, 'G3_412', 'G3', 160000, 600, 600, NOW(), NOW()),

-- P 그룹 (코너 및 측면 스탠드)
(35, 1, 'P_1', 'P', 120000, 550, 550, NOW(), NOW()),
(36, 1, 'P_2', 'P', 120000, 550, 550, NOW(), NOW()),
(37, 1, 'P_3', 'P', 120000, 550, 550, NOW(), NOW()),
(38, 1, 'P_4', 'P', 120000, 550, 550, NOW(), NOW()),
(39, 1, 'P_5', 'P', 120000, 550, 550, NOW(), NOW()),
(40, 1, 'P_6', 'P', 120000, 550, 550, NOW(), NOW()),
(41, 1, 'P_7', 'P', 120000, 550, 550, NOW(), NOW()),
(42, 1, 'P_8', 'P', 120000, 550, 550, NOW(), NOW()),
(43, 1, 'P_9', 'P', 120000, 550, 550, NOW(), NOW()),
(44, 1, 'P_10', 'P', 120000, 550, 550, NOW(), NOW()),
(45, 1, 'P_11', 'P', 120000, 550, 550, NOW(), NOW()),
(46, 1, 'P_12', 'P', 120000, 550, 550, NOW(), NOW()),
(47, 1, 'P_13', 'P', 120000, 550, 550, NOW(), NOW()),
(48, 1, 'P_14', 'P', 120000, 550, 550, NOW(), NOW()),
(49, 1, 'P_15', 'P', 120000, 550, 550, NOW(), NOW()),
(50, 1, 'P_16', 'P', 120000, 550, 550, NOW(), NOW()),
(51, 1, 'P_17', 'P', 120000, 550, 550, NOW(), NOW()),
(52, 1, 'P_18', 'P', 120000, 550, 550, NOW(), NOW()),
(53, 1, 'P_19', 'P', 120000, 550, 550, NOW(), NOW()),
(54, 1, 'P_20', 'P', 120000, 550, 550, NOW(), NOW()),

-- R 그룹 (후면 및 측면 상단)
(55, 1, 'R_1', 'R', 100000, 600, 600, NOW(), NOW()),
(56, 1, 'R_2', 'R', 100000, 600, 600, NOW(), NOW()),
(57, 1, 'R_3', 'R', 100000, 600, 600, NOW(), NOW()),
(58, 1, 'R_4', 'R', 100000, 600, 600, NOW(), NOW()),
(59, 1, 'R_5', 'R', 100000, 600, 600, NOW(), NOW()),
(60, 1, 'R_6', 'R', 100000, 600, 600, NOW(), NOW()),
(61, 1, 'R_7', 'R', 100000, 600, 600, NOW(), NOW()),
(62, 1, 'R_8', 'R', 100000, 600, 600, NOW(), NOW()),
(63, 1, 'R_9', 'R', 100000, 600, 600, NOW(), NOW()),
(64, 1, 'R_10', 'R', 100000, 600, 600, NOW(), NOW()),
(65, 1, 'R_11', 'R', 100000, 600, 600, NOW(), NOW()),

-- S 그룹 (최측면 및 후면)
(66, 1, 'S_1', 'S', 80000, 500, 500, NOW(), NOW()),
(67, 1, 'S_2', 'S', 80000, 500, 500, NOW(), NOW()),
(68, 1, 'S_3', 'S', 80000, 500, 500, NOW(), NOW()),
(69, 1, 'S_4', 'S', 80000, 500, 500, NOW(), NOW()),
(70, 1, 'S_5', 'S', 80000, 500, 500, NOW(), NOW()),
(71, 1, 'S_6', 'S', 80000, 500, 500, NOW(), NOW()),
(72, 1, 'S_7', 'S', 80000, 500, 500, NOW(), NOW()),
(73, 1, 'S_8', 'S', 80000, 500, 500, NOW(), NOW()),
(74, 1, 'S_9', 'S', 80000, 500, 500, NOW(), NOW()),
(75, 1, 'S_10', 'S', 80000, 500, 500, NOW(), NOW()),
(76, 1, 'S_11', 'S', 80000, 500, 500, NOW(), NOW()),
(77, 1, 'S_12', 'S', 80000, 500, 500, NOW(), NOW()),

-- A 그룹 (최후면 및 막힌 시야 구역)
(78, 1, 'A_1', 'A', 50000, 444, 444, NOW(), NOW()),
(79, 1, 'A_2', 'A', 50000, 444, 444, NOW(), NOW()),
(80, 1, 'A_3', 'A', 50000, 444, 444, NOW(), NOW()),
(81, 1, 'A_4', 'A', 50000, 444, 444, NOW(), NOW()),
(82, 1, 'A_5', 'A', 50000, 444, 444, NOW(), NOW()),
(83, 1, 'A_6', 'A', 50000, 444, 444, NOW(), NOW()),
(84, 1, 'A_7', 'A', 50000, 444, 444, NOW(), NOW()),
(85, 1, 'A_8', 'A', 50000, 444, 444, NOW(), NOW()),
(86, 1, 'A_9', 'A', 50000, 448, 448, NOW(), NOW()); -- 마지막 섹션은 448석으로 조정하여 정확히 50,000석

-- 4. 50000개 좌석 데이터 생성
SET SESSION cte_max_recursion_depth = 100000;

INSERT IGNORE INTO seat (id, row_name, col_name, venue_id, created_at, updated_at)
SELECT n,
       CHAR(65 + FLOOR((n - 1) / 100)), -- A부터 시작하는 행 이름 (최대 500행)
       LPAD((n - 1) % 100 + 1, 3, '0'), -- 001-100 형식의 열 번호
       1, -- venue_id
       NOW(),
       NOW()
FROM (WITH RECURSIVE numbers(n) AS (SELECT 1
                                    UNION ALL
                                    SELECT n + 1
                                    FROM numbers
                                    WHERE n < 50000)
      SELECT n
      FROM numbers) AS seat_numbers;

-- 5. 100만명 회원 데이터 생성
SET SESSION cte_max_recursion_depth = 1000000;

INSERT IGNORE INTO member (id, name, email, password, created_at, updated_at)
WITH RECURSIVE numbers(n) AS (SELECT 1
                              UNION ALL
                              SELECT n + 1
                              FROM numbers
                              WHERE n < 1000000)
SELECT n,
       CONCAT('테스트유저', LPAD(n, 7, '0')),
       CONCAT('user', n, '@test.com'),
       '$2a$10$dummy.hashed.password',
       NOW(),
       NOW()
FROM numbers;

-- 6. 인벤토리 데이터 생성
INSERT IGNORE INTO inventory (show_id, grade, total_count, available_count)
VALUES
    (1, 'G1', 8000, 8000),      -- G1 그룹: 10개 섹션 x 800석
    (1, 'G2', 7200, 7200),      -- G2 그룹: 12개 섹션 x 600석
    (1, 'G3', 7200, 7200),      -- G3 그룹: 12개 섹션 x 600석
    (1, 'P', 11000, 11000),     -- P 그룹: 20개 섹션 x 550석
    (1, 'R', 6600, 6600),       -- R 그룹: 11개 섹션 x 600석
    (1, 'S', 6000, 6000),       -- S 그룹: 12개 섹션 x 500석
    (1, 'A', 4000, 4000);       -- A 그룹: 9개 섹션 x 444석 (마지막 448석)

-- 7. 티켓 데이터 생성
INSERT IGNORE INTO ticket (id, show_id, seat_id, section_id, price, status, created_at, updated_at)
WITH cumulative_seats AS (
    SELECT
        id as section_id,
        section_name,
        section_type,
        base_price,
        total_seats,
        (SELECT COALESCE(SUM(s2.total_seats), 0)
         FROM section s2
         WHERE s2.show_id = 1 AND s2.id < s1.id) + 1 as start_seat_id,
        (SELECT COALESCE(SUM(s2.total_seats), 0)
         FROM section s2
         WHERE s2.show_id = 1 AND s2.id <= s1.id) as end_seat_id
    FROM section s1
    WHERE s1.show_id = 1
    ORDER BY s1.id
)
SELECT
    s.id,
    1, -- show_id
    s.id, -- seat_id
    cs.section_id,
    cs.base_price,
    'AVAILABLE',
    NOW(),
    NOW()
FROM seat s
         JOIN cumulative_seats cs ON s.id BETWEEN cs.start_seat_id AND cs.end_seat_id
WHERE s.id <= 50000; -- 생성된 좌석 수 만큼만 티켓 생성

-- 8. 데이터 생성 확인
SELECT '=== Load Test 데이터 생성 최종 확인 ===' as info;

SELECT '공연장 정보' as table_name, CONCAT('총 ', COUNT(*), '개 장소') as count
FROM venue
UNION ALL
SELECT '공연 정보', CONCAT('총 ', COUNT(*), '개 공연')
FROM `show`
UNION ALL
SELECT '섹션 정보', CONCAT('총 ', COUNT(*), '개 섹션 (G1:10, G2:12, G3:12, P:20, R:11, S:12, A:9)')
FROM section
UNION ALL
SELECT '좌석 정보', CONCAT('총 ', COUNT(*), '개 좌석')
FROM seat
UNION ALL
SELECT '회원 정보', CONCAT('총 ', COUNT(*), '명')
FROM member
UNION ALL
SELECT '티켓 정보', CONCAT('총 ', COUNT(*), '매 (AVAILABLE: ', SUM(CASE WHEN status = 'AVAILABLE' THEN 1 ELSE 0 END), ')')
FROM ticket
UNION ALL
SELECT '인벤토리 정보', CONCAT('총 ', COUNT(*), '개 그룹')
FROM inventory;

-- 9. 그룹별 좌석 배분
SELECT
    section_type as 등급,
    COUNT(*) as 섹션수,
    SUM(total_seats) as 총좌석수,
    ROUND(SUM(total_seats) * 100.0 / 50000, 1) as 비율,
    MIN(base_price) as 최저가격,
    MAX(base_price) as 최고가격
FROM section
WHERE show_id = 1
GROUP BY section_type
ORDER BY FIELD(section_type, 'G1', 'G2', 'G3', 'P', 'R', 'S', 'A');

-- 10. 공연별 섹션 상세 정보
SELECT
    s.title as 공연명,
    sec.section_name as 섹션명,
    sec.section_type as 등급,
    sec.total_seats as 좌석수,
    sec.available_seats as 예매가능,
    FORMAT(sec.base_price, 0) as 가격
FROM section sec
         JOIN `show` s ON sec.show_id = s.id
ORDER BY FIELD(sec.section_type, 'G1', 'G2', 'G3', 'P', 'R', 'S', 'A'), sec.id;

-- 11. 티켓 상태별 집계
SELECT
    show_id,
    status,
    COUNT(*) as ticket_count,
    FORMAT(MIN(price), 0) as min_price,
    FORMAT(MAX(price), 0) as max_price,
    FORMAT(AVG(price), 0) as avg_price
FROM ticket
GROUP BY show_id, status
ORDER BY show_id, status;

SELECT 'Load Test 데이터 생성 완료! (브루노 마스 콘서트 50,000석 배분 완료)' as message;