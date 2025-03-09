# Zipsoon (집순)
> 집 순위를 손쉽게 매겨봅시다! 집순

Zipsoon은 사용자의 설정에 따라 부동산 매물에 점수를 매겨주는, 집 구하기 서비스입니다.

<br><br>

## 0. 빠르게 구경하세요!

- [↗️ 웹앱 구경하기](https://shiny-goldfish-wgw9rqjqw9435x54-5500.app.github.dev/)
- [↗️ swagger 구경하기](https://shiny-goldfish-wgw9rqjqw9435x54-8080.app.github.dev/swagger-ui/index.html)

❗️위 링크는 codespace에 의해 제공됩니다. 비용이나 보안 문제로 codespace가 닫힐 경우 접근할 수 없습니다.

<br><br>

## 1. Features & Screens

<table>
  <tr>
    <th>메인 화면</th>
    <th>매물 검색 결과</th>
    <th>매물 상세 정보</th>
    <th>사용자 설정</th>
  </tr>
  <tr valign="top">
    <td>
      <img src="/assets/images/guest-screen.png" alt="메인 화면(게스트)"><br>
      • 게스트: 매물 정보만 제공
    </td>
    <td>
      <img src="/assets/images/user-screen.png" alt="메인 화면(회원)"><br>
      • 사용자: 매물 점수 제공<br>
      > 사용자는 여러가지 "점수 계산기"를 선택할 수 있음<br>
      > 선택된 계산기 중 상위 3개 점수 노출
    </td>
    <td>
      <img src="/assets/images/detail-screen.png" alt="매물 상세보기 화면"><br>
      • 매물 선택: 종합 평점, 상세 정보 제공<br>
      > 계산기별 상세 점수 제공
    </td>
    <td>
      <img src="/assets/images/settings-screen.png" alt="유저 정보 화면"><br>
      • 사용자 설정: 계산기 개인화<br>
      > 계산기별 활성화-제외 가능
    </td>
  </tr>
</table>


<img src="/assets/images/blueprint.png" alt="기획 화면"><br>

## 3. Tech Stack

**Core**
- Java 17
- Spring Boot 3.4.x
- Spring Security with JWT Authentication
- MyBatis

**Database**
- PostgreSQL with PostGIS extension

**Testing**
- JUnit 5
- Mockito for unit testing

**Build & Development**
- Gradle 8.x
- Docker
- Docker Compose

**API Documentation**
- Swagger/OpenAPI

## 4. System Architecture
```mermaid
flowchart TD
    NaverLand[부동산 중개 사이트] -->|batch| ZipsoonBatch
    PublicData[공공데이터] -->|batch| ZipsoonBatch
    ZipsoonBatch -->|Insert/Update| DB[(PostgreSQL + PostGIS)]
    DB -->|Read| ZipsoonApp
    ZipsoonApp <-->|REST API| 유저
    
    subgraph " "
        ZipsoonBatch
    end
    
    subgraph " "
        ZipsoonApp
    end
```

## 5. ERD
```mermaid
erDiagram
    PropertySnapshot {
        bigint id PK
        varchar platform_type "네이버/직방/다방 등"
        varchar platform_id "플랫폼별 매물 ID"
        jsonb raw_data "원본 데이터 보관"
        varchar prop_name "매물명"
        varchar prop_type "아파트/오피스텔/빌라"
        varchar trade_type "매매/전세/월세"
        numeric price "매매가/보증금"
        numeric rent_price "월세"
        numeric area_meter "전용면적(㎡)"
        numeric area_pyeong "전용면적(평)"
        geometry location "위치(PostGIS Point)"
        varchar address "주소"
        varchar[] tags "태그 목록"
        varchar dong_code "법정동 코드"
        timestamp created_at
    }

    User {
        bigint id PK
        varchar email
        varchar password
        varchar name
        varchar provider
        timestamp created_at
    }

    UserFilters {
        bigint id PK
        bigint user_id FK
        varchar name
        varchar description
        boolean is_active
        timestamp created_at
    }

    Filter {
        bigint id PK
        bigint filter_set_id FK
        varchar category "시설/환경/통근"
        varchar name "필터명"
        integer priority "우선순위"
        decimal weight "가중치"
        jsonb config "필터별 설정"
    }

    ScoreSnapshot {
        bigint id PK
        bigint property_id FK
        bigint filter_set_id FK
        decimal score "0-10점"
        jsonb details "상세 점수"
        timestamp created_at
    }

    PropertySnapshot ||--o{ Filter : has
    User ||--o{ UserFilters : owns
    UserFilters ||--o{ FilterOption : contains
    UserFilters ||--o{ ScoreSnapshot : produces
```
