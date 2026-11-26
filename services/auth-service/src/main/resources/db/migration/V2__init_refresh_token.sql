CREATE TABLE refresh_tokens
(
    id         UUID PRIMARY KEY,
    token      TEXT        NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version    BIGINT,
    revoked    BOOLEAN     NOT NULL DEFAULT FALSE,
    user_id    UUID        NOT NULL,

    CONSTRAINT uk_refresh_tokens_token UNIQUE (token),
    CONSTRAINT fk_refresh_tokens_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_refresh_tokens_expires_at ON refresh_tokens (expires_at);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
