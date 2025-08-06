# 공연 예매 시스템

> 뮤지컬, 공연, 콘서트 등 다양한 이벤트의 좌석을 예매할 수 있는 서비스입니다.
대량의 트래픽에서도 좌석 중복/경합 없이 안정적으로 처리되는 구조를 구현하기 위해 시작했습니다.
특히 Redis Stream 기반 Lock-Free 구조와 자체 Rebalancing 시스템을 통해 확장성과 처리 안정성을 확보했습니다.


## Installation & Requirements

<div>
  <img src="https://img.shields.io/badge/Java17-red?style=for-the-badge&logo=Java&logoColor=white"/></a> 
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Mysql-4479A1?style=for-the-badge&logo=MySql&logoColor=white"/></a>
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
<img src="https://img.shields.io/badge/JPA-FF3621?style=for-the-badge&logo=Databricks&logoColor=white">
</div>

- Java 17+
- Gradle 7.5+
- **Docker** (for Testcontainers)


## Features

- 실시간 좌석 예매 처리 (Lock-Free 메시징 기반)
- Redis Stream 기반 비동기 메시지 처리 구조
- 섹션 단위 파티셔닝으로 수평 확장성 확보
- Consumer Group 없이 순서 보장된 메시지 처리
- 리밸런싱 시스템 자체 구현 (Consistent Hashing 기반)
- Idempotency-Key를 통한 중복 예매 방지
- DB Polling 기반 비동기 응답 처리




## ERD

<img src="https://github.com/user-attachments/assets/d27a27fa-b6d1-4544-8430-a0a04b679785" width="600"/>


## System Architecture

<img width="800" height="2087" alt="image" src="https://github.com/user-attachments/assets/951f06ef-0d69-41dd-86a0-9e261994cafa" />


```
1. 섹션별 샤딩을 통해 Redis Stream과 API 서버의 수평적 확장성 확보

2. 하나의 Stream은 하나의 Consumer만 담당하여 순서 보장 및 처리 중복 방지

3. Lock-Free 방식으로 DB 병목 현상 최소화 및 처리 성능 향상
```

### 메시지 처리 흐름

1. 사용자가 좌석 예매 요청을 보냄 (POST /bookings)
2. API 서버는 요청을 Redis Stream에 비동기 저장
3. BookingConsumer가 해당 섹션의 메시지를 단일 스레드로 소비
4. DB에 중복 검사 → 예매 처리
5. 사용자는 예매 결과를 별도 조회 API로 확인 (GET /bookings/{requestId})

> 예매 상태는 DB에 저장되므로 서버 재시작, 장애 상황에서도 유실 없이 관리됩니다.

---

## Rebalancing Strategy


<img width="1989" height="1453" alt="image" src="https://github.com/user-attachments/assets/74c6016e-64cd-47ff-ba45-fbcd0f6fd137" />


### 리밸런싱 과정

1. 새로운 Consumer 등록/종료 시 리밸런싱 트리거
2. Redisson 락으로 다중 노드 충돌 방지
3. ConsistentHash로 최소 파티션 재배치
4. Redis Stream으로 리밸런싱 이벤트 브로드캐스트
5. 각 Consumer는 자신에게 할당된 파티션만 소비
