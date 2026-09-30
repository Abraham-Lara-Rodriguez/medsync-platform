CREATE TABLE doctors
(
    id                   UUID PRIMARY KEY,
    first_name           VARCHAR(100) NOT NULL,
    last_name            VARCHAR(100) NOT NULL,
    specialty            VARCHAR(100) NOT NULL,
    medical_license      TEXT         NOT NULL,
    email                TEXT         NOT NULL,
    phone                TEXT         NOT NULL,
    medical_license_hash VARCHAR(64)  NOT NULL UNIQUE,
    email_hash           VARCHAR(64)  NOT NULL UNIQUE,
    phone_hash           VARCHAR(64)  NOT NULL UNIQUE,
    status               VARCHAR(20)           DEFAULT 'ACTIVE' NOT NULL,
    version              BIGINT                DEFAULT 0 NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctor_specialty ON doctors (specialty);
CREATE INDEX idx_doctor_status ON doctors (status);
CREATE INDEX idx_doctor_email_hash ON doctors (email_hash);
CREATE INDEX idx_doctor_medical_license_hash ON doctors (medical_license_hash);
CREATE INDEX idx_doctor_phone_hash ON doctors (phone_hash);
