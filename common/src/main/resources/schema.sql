CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE estate_snapshot (
    id bigserial PRIMARY KEY,
    platform_type varchar(20) NOT NULL,
    platform_id varchar(50) NOT NULL,
    raw_data jsonb NOT NULL,

    estate_name varchar(100),
    estate_type varchar(20),
    trade_type varchar(20),
    price numeric(15,2),
    rent_price numeric(15,2),
    area_meter numeric(10,2),
    area_pyeong numeric(10,2),
    location geometry(Point, 4326) NOT NULL,
    address varchar(200),
    tags varchar[],
    dong_code varchar(10),
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX estate_snapshot_location_idx ON estate_snapshot USING GIST (location);

CREATE TABLE app_user (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    email_verified BOOLEAN NOT NULL DEFAULT false,
    name VARCHAR(255) NOT NULL,
    image_url VARCHAR(2048),
    role VARCHAR(20) NOT NULL,
    provider VARCHAR(20),
    provider_id VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_provider_provider_id ON app_user (provider, provider_id);
CREATE UNIQUE INDEX uk_provider_provider_id ON app_user (provider, provider_id);

CREATE TABLE score_type (
    id serial PRIMARY KEY,
    name varchar(50) NOT NULL UNIQUE,
    description text,
    active boolean DEFAULT true,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO score_type (name, description) VALUES
('공원', '공원의 개수와 규모를 점수화합니다');


CREATE TABLE estate_score (
    id bigserial PRIMARY KEY,
    estate_snapshot_id bigint NOT NULL,
    score_type_id int NOT NULL,
    score numeric(5,2) NOT NULL,
    created_at timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (estate_snapshot_id) REFERENCES estate_snapshot(id),
    FOREIGN KEY (score_type_id) REFERENCES score_type(id)
);

CREATE INDEX idx_estate_location_score_estate_id ON estate_score(estate_snapshot_id);
CREATE INDEX idx_estate_location_score_type_id ON estate_score(score_type_id);
