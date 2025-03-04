CREATE EXTENSION IF NOT EXISTS postgis;

-- 최신 부동산 정보 테이블: 각 매물의 최신 정보만 저장
CREATE TABLE estate (
    id bigserial PRIMARY KEY,                           -- 고유 식별자
    platform_type varchar(20) NOT NULL,                 -- 매물 플랫폼 유형 (네이버, 직방 등)
    platform_id varchar(50) NOT NULL,                   -- 플랫폼에서의 매물 ID
    raw_data jsonb NOT NULL,                            -- 원본 API 응답 데이터
    estate_name varchar(100),                           -- 매물 이름
    estate_type varchar(20),                            -- 매물 유형 (아파트, 오피스텔 등)
    trade_type varchar(20),                             -- 거래 유형 (매매, 전세, 월세 등)
    price numeric(15,2),                                -- 매매가
    rent_price numeric(15,2),                           -- 임대료 (전세금/월세)
    area_meter numeric(10,2),                           -- 면적 (제곱미터)
    area_pyeong numeric(10,2),                          -- 면적 (평)
    location geometry(Point, 4326) NOT NULL,            -- 위치 좌표 (WGS84)
    address varchar(200),                               -- 주소
    image_urls varchar[] DEFAULT '{}',                  -- 이미지 URL 배열
    tags varchar[],                                     -- 태그 배열 (특징, 키워드 등)
    dong_code varchar(10),                              -- 법정동 코드
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 레코드 생성 시간
);

CREATE INDEX estate_location_idx ON estate USING GIST (location);  -- 공간 검색을 위한 인덱스
CREATE UNIQUE INDEX estate_platform_idx ON estate (platform_type, platform_id);  -- 매물 중복 방지를 위한 인덱스

-- 과거 부동산 정보 스냅샷 테이블: 매물 이력 관리용
CREATE TABLE estate_snapshot (
    id bigserial PRIMARY KEY,                           -- 고유 식별자
    platform_type varchar(20) NOT NULL,                 -- 매물 플랫폼 유형
    platform_id varchar(50) NOT NULL,                   -- 플랫폼에서의 매물 ID
    raw_data jsonb NOT NULL,                            -- 원본 API 응답 데이터
    estate_name varchar(100),                           -- 매물 이름
    estate_type varchar(20),                            -- 매물 유형
    trade_type varchar(20),                             -- 거래 유형
    price numeric(15,2),                                -- 매매가
    rent_price numeric(15,2),                           -- 임대료
    area_meter numeric(10,2),                           -- 면적 (제곱미터)
    area_pyeong numeric(10,2),                          -- 면적 (평)
    location geometry(Point, 4326) NOT NULL,            -- 위치 좌표
    address varchar(200),                               -- 주소
    image_urls varchar[] DEFAULT '{}',                  -- 이미지 URL 배열
    tags varchar[],                                     -- 태그 배열
    dong_code varchar(10),                              -- 법정동 코드
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 레코드 생성 시간
);

-- 점수 유형 테이블: 매물 평가 기준 정의
CREATE TABLE score_type (
    id serial PRIMARY KEY,                               -- 고유 식별자
    name varchar(50) NOT NULL UNIQUE,                    -- 점수 유형 이름 (공원, 지하철 등)
    description text,                                     -- 점수 유형 설명
    active boolean DEFAULT true,                          -- 활성화 여부
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 생성 시간
);

-- 점수 유형 예제: 공원 접근성
INSERT INTO score_type (name, description) VALUES
('공원', '공원의 개수와 규모를 점수화합니다');

-- 최신 부동산 점수 테이블: 각 매물의 최신 점수만 저장
CREATE TABLE estate_score (
    id bigserial PRIMARY KEY,                           -- 고유 식별자
    estate_id bigint NOT NULL,                          -- estate 테이블 참조
    score_type_id int NOT NULL,                         -- score_type 테이블 참조
    raw_score numeric(5,2) NOT NULL,                    -- 원시 점수 (계산된 실제 값)
    normalized_score numeric(5,2),                      -- 정규화된 점수 (0-10 점)
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  -- 생성 시간

    FOREIGN KEY (estate_id) REFERENCES estate(id),      -- estate 테이블 외래키
    FOREIGN KEY (score_type_id) REFERENCES score_type(id)  -- score_type 테이블 외래키
);

CREATE INDEX idx_estate_score_estate_id ON estate_score(estate_id);  -- 매물 조회 인덱스
CREATE INDEX idx_estate_score_type_id ON estate_score(score_type_id);  -- 점수 조회 인덱스
CREATE UNIQUE INDEX uk_estate_score_estate_type ON estate_score(estate_id, score_type_id);  -- 매물-점수 제약조건

-- 과거 부동산 점수 스냅샷 테이블: 매물 점수 이력 관리용
CREATE TABLE estate_score_snapshot (
    id bigserial PRIMARY KEY,                           -- 고유 식별자
    estate_snapshot_id bigint NOT NULL,                 -- estate_snapshot 테이블 참조
    score_type_id int NOT NULL,                         -- score_type 테이블 참조
    raw_score numeric(5,2) NOT NULL,                    -- 원시 점수
    normalized_score numeric(5,2),                      -- 정규화된 점수
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 생성 시간
);

-- 사용자 정보 테이블: 앱 사용자 관리
CREATE TABLE app_user (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,  -- 고유 식별자
    email VARCHAR(255) NOT NULL UNIQUE,                  -- 이메일 (로그인 ID)
    email_verified BOOLEAN NOT NULL DEFAULT false,       -- 이메일 인증 여부
    name VARCHAR(255) NOT NULL,                          -- 사용자 이름
    image_url VARCHAR(2048),                             -- 프로필 이미지 URL
    role VARCHAR(20) NOT NULL,                           -- 역할 (USER, ADMIN 등)
    created_at TIMESTAMP NOT NULL,                       -- 계정 생성 시간
    updated_at TIMESTAMP NOT NULL                        -- 계정 정보 수정 시간
);

-- 법정동 코드 테이블: 시/군/구/동 코드 정보
CREATE TABLE dongcodes (
    법정동코드 varchar(255) PRIMARY KEY,
    법정동명 varchar(255) NOT NULL,
    폐지여부 varchar(50) NOT NULL
);