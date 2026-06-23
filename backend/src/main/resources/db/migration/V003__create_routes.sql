CREATE TABLE IF NOT EXISTS routes (
    id BIGSERIAL PRIMARY KEY,
    owner_user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(255) NOT NULL,
    route_type VARCHAR(20) NOT NULL,
    transport_mode VARCHAR(20) NOT NULL,
    distance_km DOUBLE PRECISION,
    duration_minutes INTEGER,
    elevation_gain_meters INTEGER,
    scenic_score INTEGER,
    visibility VARCHAR(20) NOT NULL DEFAULT 'PRIVATE',
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
