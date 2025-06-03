# 공연 예매 시스템

> 뮤지컬, 공연, 콘서트 등 다양한 이벤트를 예약할 수 있는 서비스입니다. <br> 좌석 예약이라는 기능을 깊게 파고 들기 위해 시작한 프로젝트입니다


## Stack

<div>
  <img src="https://img.shields.io/badge/Java17-red?style=for-the-badge&logo=Java&logoColor=white"/></a> 
  <img src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white"/>
  <img src="https://img.shields.io/badge/Mysql-4479A1?style=for-the-badge&logo=MySql&logoColor=white"/></a>
<img src="https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=Redis&logoColor=white">
<img src="https://img.shields.io/badge/JPA-FF3621?style=for-the-badge&logo=Databricks&logoColor=white">
</div>

---

## ERD

<img src="https://github.com/user-attachments/assets/d27a27fa-b6d1-4544-8430-a0a04b679785" width="932"/>

---

## System Architecture

<img width="953" alt="image" src="https://github.com/user-attachments/assets/b0b45c9a-760a-4ff1-8c4c-46eccd7a0612" width="900" height="600"/>

```
1. 섹션별 샤딩을 통해 Redis Stream과 API 서버의 수평적 확장성 확보

2. 하나의 Stream은 하나의 Consumer만 담당하여 순서 보장 및 처리 중복 방지

3. Lock-Free 방식으로 DB 병목 현상 최소화 및 처리 성능 향상
```
