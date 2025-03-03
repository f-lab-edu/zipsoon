# ZIPSOON 프로젝트 가이드

## 프로젝트 개요
ZIPSOON은 집 순위를 매기는 자취생용 부동산 매물 추천 서비스입니다. 네이버 부동산 API에서 매물 정보를 수집하고 위치 데이터와 사용자 선호도에 따라 매물에 점수를 매깁니다.

## 시스템 아키텍처
- **API 서버**: 매물 검색 및 사용자 인증 API 제공
- **Batch 서버**: 매물 수집, 점수 계산, 정규화 작업 처리
- **PostgreSQL + PostGIS**: 공간 데이터 활용 지원

## API 모듈 워크플로우

### 주요 엔드포인트
1. **인증 API** (`/api/v1/auth/*`)
   - `POST /api/v1/auth/signup`: 회원가입
   - `POST /api/v1/auth/login`: 로그인 (JWT 토큰 발급)

2. **매물 API** (`/api/v1/estates/*`)
   - `GET /api/v1/estates/map`: 지도 뷰포트 내 매물 조회
   - `GET /api/v1/estates/{id}`: 개별 매물 상세 정보 조회

3. **사용자 API** (`/api/v1/users/*`)
   - `DELETE /api/v1/users/me`: 회원 탈퇴

### 인증 흐름
1. 사용자 회원가입/로그인 → JWT 토큰 발급
2. 요청 헤더에 JWT 토큰 포함 → `JwtAuthenticationFilter`에서 검증
3. 인증 성공 → `SecurityContext`에 인증 정보 저장 → 요청 처리

### 매물 검색 흐름
1. 지도 뷰포트 정보(좌표, 줌 레벨) 수신
2. PostGIS 공간 검색으로 해당 영역 내 매물 조회
3. 필요시 매물별 점수 기준 정렬 후 결과 반환

## Batch 모듈 워크플로우

### Job 구성
1. **Estate Collection Job**
   - 법정동 코드 기반 네이버 부동산 API 호출
   - 매물 정보 수집 및 `estate_snapshot` 테이블에 저장

2. **Score Source Job**
   - 공원, 지하철 등 점수 계산에 필요한 데이터 수집
   - 외부 데이터 소스(CSV 등)에서 정보 로드 및 DB 저장

3. **Score Calculation Job**
   - 매물별 원시 점수(raw score) 계산
   - 다양한 `ScoreCalculator` 구현체를 통해 점수 산출

4. **Score Normalization Job**
   - 원시 점수를 0-10 사이 정규화된 값으로 변환
   - 매물 간 상대적 순위 부여

### Job 실행 순서
1. `BatchJobRunner`가 애플리케이션 시작 시 `ScheduleConfig`의 작업 트리거
2. Estate Job → Source Job → Score Job → Normalize Job 순차 실행
3. 각 Job 완료 후 다음 Job 실행 (에러 발생 시 로그 기록)

## 데이터 처리 흐름

### 매물 수집 및 저장
1. 네이버 부동산 API 호출 (`NaverLandClient`)
2. 응답 JSON → `EstateSnapshot` 객체 변환 (`NaverEstateCollector`)
3. DB 저장 (위치 정보는 PostGIS Point 타입으로 저장)

### 점수 계산 프로세스
1. 매물 위치 기반 공원, 지하철 등 주변 시설 조회
2. 각 요소별 점수 계산 (거리, 개수, 규모 등 고려)
3. 점수 정규화로 매물 간 비교 가능한 척도 제공
4. 최종 점수는 `estate_score` 테이블에 저장

## 주요 구현 패턴

### Repository 패턴
- MyBatis Mapper 인터페이스 → Repository 클래스 → Service 계층
- XML 기반 SQL 정의 (복잡한 공간 쿼리 지원)

### 예외 처리
- `ServiceException` + `ErrorCode` 조합으로 일관된 에러 응답
- `GlobalExceptionHandler`에서 통합 처리

### 인증 보안
- JWT 기반 무상태 인증 구현
- 토큰 검증, 갱신, 만료 처리 포함

### 배치 처리
- Chunk 기반 처리로 대용량 데이터 관리
- Reader → Processor → Writer 파이프라인
- 스케줄링된 작업 체인으로 데이터 파이프라인 구성

## 개발 환경 설정

### 로컬 개발 준비
1. Docker 환경 구성: `docker-compose -f docker-compose.local.yml up -d`
2. API 서버 실행: `./gradlew api:bootRun --args='--spring.profiles.active=local'`
3. Batch 서버 실행: `./gradlew batch:bootRun --args='--spring.profiles.active=local'`

### 테스트 실행
```bash
# API 모듈 테스트
./gradlew api:test

# Batch 모듈 테스트
./gradlew batch:test
```

## 핵심 파일 경로
- 애플리케이션 설정: `api/src/main/resources/application*.yml`
- 인증 관련: `api/src/main/java/com/zipsoon/api/auth`
- 부동산 매물: `api/src/main/java/com/zipsoon/api/estate`
- 배치 작업: `batch/src/main/java/com/zipsoon/batch`
- DB 스키마: `common/src/main/resources/schema.sql`

## 트러블슈팅 가이드
- JWT 인증 문제: 토큰 포맷, 서명 키, 만료 시간 확인
- 공간 쿼리 이슈: SRID 일치 여부 및 PostGIS 함수 사용법 검토
- 배치 작업 실패: Job 로그 확인 및 Step 별 실행 상태 분석
- API 응답 오류: `ErrorCode` 확인 및 글로벌 예외 핸들러 로직 검토

## Git 워크플로우 및 커밋 규칙

### 기본 구조
이슈, 브랜치, 커밋은 다음과 같은 계층 구조로 관리됩니다:
```
이슈
└── 브랜치
    ├── 커밋...
    └── 커밋
```

### 명명 규칙
1. **이슈 형식**: `#이슈번호 이슈이름`
   - 예: `#21 PostgreSQL + PostGIS 연동`

2. **브랜치 형식**: `type/도메인/#이슈번호-기능`
   - 예: `feature/common/#21-database-setup`

3. **커밋 형식**: `#이슈번호 type. 메시지`
   - 예: `#21 refactor. PostgreSQL UPSERT 활용하도록 리포지토리 로직 개선`
   - 메시지는 한국어로 작성

### 워크플로우 프로세스
1. 이슈 생성 후 해당 이슈 번호로 브랜치 생성
2. 개발 완료 후 관련 변경사항은 관심사별로 분리하여 커밋
3. 기능 완료 후 develop 브랜치로 PR 생성
4. 코드 리뷰 후 develop에 병합
5. 전체 기능 테스트 후 main 브랜치로 병합

### 주요 Git 작업 가이드
- 항상 develop 브랜치에서 새 브랜치 생성 시작
- rename 작업은 반드시 `git mv` 명령어 사용
- 커밋 메시지는 작업 내용을 명확히 설명
- 하나의 커밋은 하나의 논리적 변경사항만 포함

### 브랜치 타입 목록
- `feature`: 새로운 기능 개발
- `bugfix`: 버그 수정
- `hotfix`: 긴급 수정 사항
- `refactor`: 코드 리팩토링
- `chore`: 빌드 작업, 의존성 업데이트 등
- `docs`: 문서 업데이트