# Zipsoon (집순)
네이버 부동산 데이터 기반 맞춤형 주거지 추천 서비스

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
      <img src="/images/main.png" alt="Main Screen" style="display: block; margin: 0 auto;">
      <br>
      • 라이프스타일별 맞춤 필터링<br>
      • 개인화된 가중치 설정<br>
      • 실시간 인기 매물 노출<br>
      • 교통, 교육, 편의시설 등 카테고리별 필터
    </td>
    <td style="text-align: center;">
      <img src="/images/search.png" alt="Search Result" style="display: block; margin: 0 auto;">
      <br>
      • 사용자 설정 기반 매물 점수 (10점 만점)<br>
      • 카테고리별 세부 점수 분석<br>
      • 선호도에 따른 매물 정렬<br>
      • 매물 기본 정보 요약 제공
    </td>
  </tr>
  <tr>
    <th style="text-align: center;">매물 상세 정보</th>
    <th style="text-align: center;">사용자 설정</th>
  </tr>
  <tr>
    <td style="text-align: center;">
      <img src="/images/detail.png" alt="Detail Screen" style="display: block; margin: 0 auto;">
      <br>
      • 종합 점수 상세 분석 차트<br>
      • 점수 산정 근거 설명<br>
      • 매물 및 중개사무소 정보<br>
      • 반경 내 주요 시설 및 교통 정보
    </td>
    <td style="text-align: center;">
      <img src="/images/settings.png" alt="Settings Screen" style="display: block; margin: 0 auto;">
      <br>
      • 카테고리별 중요도 설정<br>
      • 불필요 항목 제외 기능<br>
      • 관심 지역 등록<br>
      • 실시간 매물 알림 설정
    </td>
  </tr>
</table>

## 3. Tech Stack
- Java 11
- Spring Boot 2.7.x
- Mybatis
- MySQL 8.0

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

### ii) 맞춤형 추천 시스템
- 사용자 우선순위 기반 점수화 알고리즘
- 다중 요소 가중치 계산 시스템
- 선호도 기반 매물 필터링

### iii) 성능 최적화
- TBD
