-- Выполнить в нужной схеме. По умолчанию search_path PostgreSQL содержит "$user".
-- Существующие данные не удаляются. ID обоих типов выдаются sequence.
CREATE SEQUENCE IF NOT EXISTS users_id_seq AS INTEGER START WITH 1 NO CYCLE;
CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY DEFAULT nextval('users_id_seq') CHECK (id > 0),
    login TEXT NOT NULL UNIQUE CHECK (btrim(login) <> ''),
    password_hash TEXT NOT NULL
);
ALTER SEQUENCE users_id_seq OWNED BY users.id;

CREATE SEQUENCE IF NOT EXISTS routes_id_seq AS INTEGER START WITH 1 NO CYCLE;
CREATE TABLE IF NOT EXISTS routes (
    id INTEGER PRIMARY KEY DEFAULT nextval('routes_id_seq') CHECK (id > 0),
    name TEXT NOT NULL CHECK (btrim(name) <> ''),
    coordinates_x REAL NOT NULL CHECK (coordinates_x > -176 AND coordinates_x <> 'NaN'::REAL),
    coordinates_y BIGINT NOT NULL,
    creation_date TIMESTAMPTZ(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    from_x BIGINT NOT NULL,
    from_y DOUBLE PRECISION NOT NULL,
    from_z BIGINT NOT NULL,
    to_x BIGINT,
    to_y DOUBLE PRECISION,
    to_z BIGINT,
    distance BIGINT CHECK (distance > 1),
    owner_id INTEGER NOT NULL REFERENCES users(id) CHECK (owner_id > 0),
    CONSTRAINT routes_to_complete CHECK (
        (to_x IS NULL AND to_y IS NULL AND to_z IS NULL)
        OR (to_x IS NOT NULL AND to_y IS NOT NULL AND to_z IS NOT NULL)
    )
);
ALTER SEQUENCE routes_id_seq OWNED BY routes.id;
CREATE INDEX IF NOT EXISTS routes_owner_id_idx ON routes (owner_id);
