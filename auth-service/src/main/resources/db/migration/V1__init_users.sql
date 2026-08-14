CREATE TABLE users
(
    id         UUID PRIMARY KEY,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(50)  NOT NULL,
    status     VARCHAR(50)  NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version    BIGINT
);

-- Índice adicional para búsquedas por email (opcional, aunque UNIQUE ya crea uno)
CREATE INDEX idx_users_email ON users (email);