CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SEQUENCE IF NOT EXISTS property_id_seq;
CREATE SEQUENCE IF NOT EXISTS property_history_id_seq;

CREATE TABLE IF NOT EXISTS property (
    id BIGINT DEFAULT nextval('property_id_seq') PRIMARY KEY,
    platform_type VARCHAR NOT NULL,
    platform_id VARCHAR NOT NULL,
    article_name VARCHAR,
    article_status VARCHAR(2),
    real_estate_type_code VARCHAR(4),
    real_estate_type_name VARCHAR(20),
    article_real_estate_type_code VARCHAR(3),
    article_real_estate_type_name VARCHAR(20),
    trade_type_code VARCHAR(2) NOT NULL,
    trade_type_name VARCHAR(10),
    verification_type_code VARCHAR(10),
    floor_info VARCHAR(10),
    price_change_state VARCHAR(10),
    is_price_modification BOOLEAN,
    price VARCHAR,
    rent_prc VARCHAR,
    deal_or_warrant_prc VARCHAR,
    area_name VARCHAR(10),
    area1 NUMERIC,
    area2 NUMERIC,
    direction VARCHAR(10),
    article_confirm_ymd VARCHAR(8),
    representative_img_url VARCHAR(255),
    article_feature_desc TEXT,
    tags VARCHAR[] DEFAULT ARRAY[]::VARCHAR[],
    building_name VARCHAR(100),
    same_addr_cnt INTEGER DEFAULT 0,
    same_addr_direct_cnt INTEGER DEFAULT 0,
    same_addr_max_prc VARCHAR(20),
    same_addr_min_prc VARCHAR(20),
    cpid VARCHAR(20),
    cp_name VARCHAR(50),
    cp_pc_article_url VARCHAR(255),
    cp_pc_article_bridge_url VARCHAR(255),
    cp_pc_article_link_use_at_article_title_yn BOOLEAN,
    cp_pc_article_link_use_at_cp_name_yn BOOLEAN,
    cp_mobile_article_url VARCHAR(255),
    cp_mobile_article_link_use_at_article_title_yn BOOLEAN,
    cp_mobile_article_link_use_at_cp_name_yn BOOLEAN,
    is_location_show BOOLEAN,
    realtor_name VARCHAR(100),
    realtor_id VARCHAR(50),
    trade_checked_by_owner BOOLEAN,
    is_direct_trade BOOLEAN,
    is_interest BOOLEAN,
    is_complex BOOLEAN,
    detail_address VARCHAR(255),
    detail_address_yn VARCHAR(1),
    virtual_address_yn VARCHAR(1),
    is_vr_exposed BOOLEAN,
    location GEOMETRY(Point, 4326),
    status VARCHAR NOT NULL DEFAULT 'ACTIVE',
    last_checked TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    deleted_at TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS property_history (
    id BIGINT DEFAULT nextval('property_history_id_seq') PRIMARY KEY,
    property_id BIGINT NOT NULL REFERENCES property(id),
    change_type VARCHAR NOT NULL,
    before_value TEXT,
    after_value TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_property_trade_type ON property(trade_type_code);
CREATE INDEX idx_property_price_change ON property(price_change_state);
CREATE INDEX idx_property_tags ON property USING gin(tags);
CREATE INDEX idx_property_location ON property USING gist(location);

ALTER TABLE property ADD CONSTRAINT uk_property_platform
    UNIQUE (platform_type, platform_id);