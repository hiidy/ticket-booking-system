# 공연 예매 시스템

> 뮤지컬, 공연, 콘서트 등 다양한 이벤트의 좌석을 예매할 수 있는 서비스입니다.

대량의 트래픽에서도 Double Booking같은 동시성 이슈 없이 안정적으로 처리되는 구조를 목표로 했습니다.  
특히 예매 기능의 **5가지 동시성 전략을 직접 구현·비교**하고, 최적의 방안을 도출하여 확장성과 안정성을 확보했습니다.

---

## 도전 과제

공연 예매 시스템에서 가장 어려운 문제는 **"동시에 같은 좌석을 여러 명이 예매하려 할 때"** 입니다.

이 프로젝트에서 다루는 핵심 질문들은 다음과 같습니다.

### 동시성 제어

- 1만 명이 동시에 같은 좌석을 예매하면 어떻게 처리하나요?
- 그 이상의 트래픽이 들어오면 어떻게 확장하나요?
- p95, p99 응답 속도는 어떻게 되나요?
- 동시성 이슈를 해결하려면 꼭 락이 필요할까요?
- 5가지 전략 중 왜 그 전략을 최종 선택했나요?

→ [5가지 동시성 전략 비교](#5가지-동시성-전략-비교)

### 장애 대응 & 데이터 정합성

- 캐시에 없는 데이터를 대량 요청하면 DB가 터지지 않나요?
- Redis가 죽으면 어떻게 되나요?
- DB와 Redis 간 데이터 정합성은 어떻게 보장하나요?
- 메시지 유실이나 중복 처리는 어떻게 방지하나요?

→ [트러블슈팅](#트러블슈팅)

---

## 기술 스택

<div>
  <img src="https://img.shields.io/badge/Java17-red?style=for-the-badge&logo=Java&logoColor=white"/> 
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Mysql-4479A1?style=for-the-badge&logo=MySql&logoColor=white"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
  <img src="https://img.shields.io/badge/JPA-FF3621?style=for-the-badge&logo=Databricks&logoColor=white">
</div>

- Java 17+
- Gradle 7.5+
- Docker (for Testcontainers)

---

# 5가지 동시성 전략 비교

1만 건 동시 요청 부하 테스트를 통해 각 전략의 응답 시간, 처리량, 운영 복잡도를 정량 분석했습니다.

<!-- [이미지 권장] 5가지 전략 전체 흐름 비교 다이어그램 -->

## 전략 1: DB Lock

<!-- [이미지 권장] DB Lock 시퀀스 다이어그램 -->

DB만으로 동시성을 제어하는 가장 기본적인 방식입니다.

낙관적 락, 비관적 락, 직렬화 격리 수준 등 여러 방식을 시도한 결과, **비관적 락 + NOWAIT** 조합이 가장 효과적이었습니다.

예매 도메인 특성상 이미 선점된 좌석을 대기하는 것보다 빠르게 실패를 반환하고 다른 좌석을 선택하게 하는 것이 UX에 유리하기 때문입니다.

**한계**: DB 커넥션 점유 시간이 길어 처리량에 한계  
**결과**: 안정적이나 대규모 트래픽에서 확장성 부족

## 전략 2: Redis 분산 Lock

<!-- [이미지 권장] Redis Lock 시퀀스 다이어그램 -->

Redisson을 활용해 좌석별로 분산 락을 획득하는 방식입니다.

DB 락의 커넥션 점유 문제는 해결되었지만, 락 획득 후 재고 차감과 예매 저장을 모두 수행해야 하므로 여전히 DB 부하가 발생합니다.

**한계**: 락 획득 후 DB 저장까지 수행하므로 여전히 병목 존재  
**결과**: DB Lock 대비 개선되었으나 고부하 시 DB 병목

## 전략 3: Redis + 비동기 저장 ✅ 최종 채택

<!-- [이미지 권장] Redis + 비동기 저장 아키텍처 다이어그램 -->

Lua 스크립트로 Redis에서 재고를 원자적으로 차감하고 즉시 응답합니다. 예매 데이터는 Kafka를 통해 비동기로 DB에 저장하여 부하를 평탄화합니다.

사용자는 Redis 재고 차감 시점에 빠른 응답을 받고, DB 저장은 Consumer가 순차적으로 처리합니다.

**장점**: 빠른 응답 + DB 부하 평탄화  
**결과**: 응답 속도와 처리량 모두 우수, 운영 복잡도 적절

## 전략 4: Kafka 파티셔닝 (Lock-Free)

<!-- [이미지 권장] Kafka 파티셔닝 흐름도 -->

섹션 ID를 기준으로 Kafka 파티셔닝을 적용하여, 같은 섹션의 요청은 동일 파티션에서 순차 처리됩니다.

파티션 내 순서가 보장되므로 락 없이도 동시성 문제가 해결됩니다.

**장점**: Lock-Free로 락 경합 없음, 순서 보장  
**한계**: 즉시 응답 불가 (Consumer 처리 후 결과 확인), 구현 복잡도 높음

## 전략 5: Kafka Streams + RocksDB

<!-- [이미지 권장] Kafka Streams 아키텍처 다이어그램 -->

Kafka Streams를 활용하여 상태 저장소를 RocksDB(로컬)로 관리합니다. DB 접근 없이 로컬에서 데이터를 처리하므로 응답 속도가 가장 빠릅니다.

단, Kafka가 SPOF가 되며, 외부 API 호출이나 데이터 연동 시 토픽을 거쳐야 하므로 유연성이 떨어집니다.

**장점**: DB 접근 없이 가장 빠른 응답 속도  
**한계**: 운영 복잡도 높음, 외부 연동 어려움

### 성능 비교 결과

<!-- [이미지 권장] 성능 비교 그래프 (응답시간, 처리량 차트) -->

| 전략 | 응답 속도 | 처리량 | 운영 복잡도 | 채택 여부 |
|-----|---------|-------|-----------|---------|
| DB Lock | ⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐⭐ | ❌ |
| Redis Lock | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐⭐ | ❌ |
| Redis + 비동기 | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ✅ |
| Kafka Lock-Free | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | ❌ |
| Kafka Streams | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | 대규모 시 고려 |

---

# 트러블슈팅

## 1. 캐시 전략: Penetration & Stampede 방지

<!-- [이미지 권장] 캐시 전략 흐름도 (블룸필터 → 캐시 → DB) -->

### 문제
- 존재하지 않는 좌석 조회 시 매번 DB 조회 (Cache Penetration)
- 캐시 만료 시 동시 요청이 DB로 몰림 (Cache Stampede)

### 해결
- **블룸 필터**: 존재하지 않는 키 사전 차단 → 무효 요청 99% 차단
- **Double Checked Locking**: 캐시 미스 시 하나의 요청만 DB 조회 → 쿼리 99% 감소

```
요청 → 블룸필터 체크 → 캐시 조회 → (미스 시) 락 획득 → 다시 캐시 확인 → DB 조회 → 캐시 저장
```

## 2. 분산 락 안정화

<!-- [이미지 권장] Redisson Watchdog 동작 다이어그램 -->

### 문제
- 락 획득 후 처리 시간이 길어지면 락 만료 → 중복 예매 발생 가능
- 서버 장애 시 락이 해제되지 않는 문제

### 해결
- **Redisson Watchdog**: 락 보유 중 자동 연장
- **소유권 검증**: 락 해제 시 본인 소유 확인
- **결과**: 중복 예매 0건 달성

## 3. 데이터 정합성 보장

<!-- [이미지 권장] Redis-DB 정합성 보장 흐름도 -->

### 문제
- Redis 재고 차감 성공 → DB 저장 실패 시 불일치
- 네트워크 장애로 인한 중복 요청

### 해결
- **Idempotency-Key**: 동일 요청 중복 처리 방지
- **롤백 + Reconcile 로그**: 실패 시 Redis 재고 복구 및 로그 기록
- **DB Polling 기반 비동기 응답**: 최종 일관성 보장

## 4. 장애 처리 & 복구

<!-- [이미지 권장] 재시도 + DLQ 흐름도 -->

### 문제
- 메시지 처리 실패 시 유실
- 일시적 장애에 대한 재시도 전략 부재

### 해결
- **지수 백오프 재시도**: 1초 → 2초 → 4초... 점진적 재시도
- **DLQ (Dead Letter Queue)**: 최종 실패 메시지 격리 및 모니터링

---

## 프로젝트 실행 방법

### 로컬 실행 (booking-app)

`booking-app` 디렉터리만으로 MySQL/Redis를 함께 띄웁니다.

```bash
cd booking-app

# 1) DB/캐시 기동 (초기 스키마와 샘플 데이터 자동 로드)
docker compose up -d
# MySQL: 3306 (user: booking_user / pass: booking_pass)
# Redis: 6379

# 2) 애플리케이션 실행
DB_USERNAME=booking_user \
DB_PASSWORD=booking_pass \
SPRING_PROFILES_ACTIVE=local \
./gradlew bootRun

# 3) 동작 확인
curl http://localhost:8080/actuator/health
open http://localhost:8080/swagger-ui.html  # API 문서

# 4) 종료/정리
docker compose down -v
```

- 테스트: `./gradlew test` (Testcontainers 사용, Docker 필요)
- 빌드된 실행 파일: `booking-app/build/libs/booking-app-*.jar`