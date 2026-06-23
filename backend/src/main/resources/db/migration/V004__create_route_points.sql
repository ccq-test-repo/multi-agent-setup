CREATE TABLE IF NOT EXISTS route_points (
    id BIGSERIAL PRIMARY KEY,
    route_id BIGINT NOT NULL REFERENCES routes(id) ON DELETE CASCADE,
    sequence_number INTEGER NOT NULL,
    point_type VARCHAR(20) NOT NULL,
    coordinates GEOMETRY(POINT, 4326) NOT NULL,
    recorded_at TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_route_points_route_id ON route_points(route_id);
CREATE INDEX IF NOT EXISTS idx_route_points_coordinates ON route_points USING GIST(coordinates);
