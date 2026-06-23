CREATE TABLE IF NOT EXISTS user_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    display_name VARCHAR(100),
    preferred_transport_mode VARCHAR(20),
    default_roundtrip_distance_km DOUBLE PRECISION,
    prefer_nature BOOLEAN,
    avoid_traffic BOOLEAN,
    prefer_water BOOLEAN,
    prefer_forest BOOLEAN,
    prefer_viewpoints BOOLEAN
);
