CREATE EXTENSION IF NOT EXISTS postgis;

CREATE TABLE property_snapshot (
    id bigserial PRIMARY KEY,
    platform_type varchar(20) NOT NULL,
    platform_id varchar(50) NOT NULL,
    raw_data jsonb NOT NULL,

    prop_name varchar(100),
    prop_type varchar(20),
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

CREATE INDEX property_snapshot_location_idx ON property_snapshot USING GIST (location);