CREATE TABLE patients
(
    id                   UUID PRIMARY KEY,
    first_name           VARCHAR(80) NOT NULL,
    last_name            VARCHAR(80) NOT NULL,
    gender               VARCHAR(10) NOT NULL,
    birth_date           DATE        NOT NULL,
    document_number      TEXT        NOT NULL,
    phone                TEXT        NOT NULL,
    email                TEXT        NOT NULL,
    document_number_hash VARCHAR(64) NOT NULL UNIQUE,
    phone_hash           VARCHAR(64) NOT NULL UNIQUE,
    email_hash           VARCHAR(64) NOT NULL UNIQUE,
    address              VARCHAR(255),
    blood_type           VARCHAR(12) NOT NULL,
    status               VARCHAR(10) NOT NULL,
    created_at           TIMESTAMP   NOT NULL DEFAULT now(),
    updated_at           TIMESTAMP,
    version              BIGINT      NOT NULL DEFAULT 0
);