CREATE EXTENSION IF NOT EXISTS postgis;

CREATE SEQUENCE IF NOT EXISTS property_id_seq;
CREATE SEQUENCE IF NOT EXISTS property_history_id_seq;

CREATE TABLE IF NOT EXISTS property (
   id BIGINT DEFAULT nextval('property_id_seq') PRIMARY KEY,
   platform_type VARCHAR NOT NULL,
   platform_id VARCHAR NOT NULL,
   name VARCHAR,
   type VARCHAR NOT NULL,
   trade_type VARCHAR NOT NULL,
   price VARCHAR,
   area NUMERIC,
   location GEOMETRY(Point, 4326),
   address VARCHAR,
   status VARCHAR NOT NULL DEFAULT 'ACTIVE',
   last_checked TIMESTAMPTZ NOT NULL,
   created_at TIMESTAMPTZ NOT NULL,
   updated_at TIMESTAMPTZ NOT NULL,
   area_p NUMERIC,
   trade_type_code VARCHAR(2) NOT NULL,
   floor_info VARCHAR(10),
   direction VARCHAR(10),
   building_name VARCHAR(100),
   age_type VARCHAR(20),
   price_change_state VARCHAR(10),
   verification_type VARCHAR(20),
   realtor_name VARCHAR(100),
   feature_description TEXT,
   tags VARCHAR[] DEFAULT ARRAY[]::VARCHAR[],
   image_url VARCHAR(255),
   same_addr_count INTEGER DEFAULT 0,
   same_addr_max_price VARCHAR(20),
   same_addr_min_price VARCHAR(20)
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

ALTER TABLE property ADD CONSTRAINT uk_property_platform
    UNIQUE (platform_type, platform_id);