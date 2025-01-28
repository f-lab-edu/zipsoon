# Zipsoon (집순)
맞춤형 주거지 추천 서비스

## 1. 프로젝트 소개

Zipsoon은 사용자의 라이프스타일과 우선순위에 기반하여 최적의 주거지를 추천하는 서비스입니다.

## 2. Features & Screens

<table>
  <tr>
    <th style="width: 50%; text-align: center;">메인 화면</th>
    <th style="width: 50%; text-align: center;">매물 검색 결과</th>
  </tr>
  <tr>
    <td style="text-align: center;">
      <img src="/assets/images/guest-screen.png" alt="메인 화면(게스트)" style="display: block; margin: 0 auto;">
      <br>
      • <i>인기 매물</i> 순위별 노출<br>
      • 디폴트 옵션 3개로 매물 점수 계산<br>
      • 매물 가격 노출
    </td>
    <td style="text-align: center;">
      <img src="/assets/images/user-screen.png" alt="메인 화면(회원)" style="display: block; margin: 0 auto;">
      <br>
      • <i>사용자 지정 매물</i> 순위별 노출<br>
      • 사용자가 설정한 옵션들로 매물 점수 계산<br>
      • 매물 가격, 상위 3개옵션 점수 노출
    </td>
  </tr>
  <tr>
    <th style="text-align: center;">매물 상세 정보</th>
    <th style="text-align: center;">사용자 설정</th>
  </tr>
  <tr>
    <td style="text-align: center;">
      <img src="/assets/images/detail-screen.png" alt="매물 상세보기 화면" style="display: block; margin: 0 auto;">
      <br>
      • 매물 종합 평점과 세부 정보 제공<br>
      • 사용자가 선택한 옵션별 점수 노출
    </td>
    <td style="text-align: center;">
      <img src="/assets/images/settings-screen.png" alt="유저 정보 화면" style="display: block; margin: 0 auto;">
      <br>
      • 사전 제공되는 옵션 목록 제공<br>
      • 사용자 옵션 추가, 제거, 순위 변경 기능 제공
    </td>
  </tr>
</table>

## 3. Tech Stack

**Core**
- Java 17
- Spring Boot 3.2.x
- Spring Security with JWT Authentication
- MyBatis

**Database**
- PostgreSQL 15 with PostGIS extension
- Supabase

**Testing**
- JUnit 5
- Testcontainers for integration testing
- Mockito for unit testing

**Build & Development**
- Gradle 8.x
- Docker
- Docker Compose

**API Documentation**
- Swagger/OpenAPI

## 4. System Architecture
[시스템 아키텍처 다이어그램]

## 5. ERD
[ERD 다이어그램]

## 6. API 명세
[Swagger UI 링크]

## 7. Technical Challenge

### i) 실시간 매물 정보 관리
- 네이버 부동산 매물 데이터 크롤링 및 동기화
- 지역별 매물 정보 업데이트 관리

### ii) 맞춤형 필터 시스템
- 매물 점수 계산 시스템
- 옵션에 따른 매물 필터링

### iii) 추천 시스템
- (TBD) 사용자의 성별, 나이, 지역에 따른 추천 시스템

### iv) 성능 최적화
- (TBD)
