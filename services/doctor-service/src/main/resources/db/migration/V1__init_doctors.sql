CREATE TABLE doctors
(
    id              UUID PRIMARY KEY,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    specialty       VARCHAR(100) NOT NULL,
    medical_license VARCHAR(50)  NOT NULL UNIQUE,
    email           VARCHAR(100) NOT NULL UNIQUE,
    phone           VARCHAR(20) NOT NULL,
    status          VARCHAR(20)           DEFAULT 'ACTIVE' NOT NULL,
    version         BIGINT                DEFAULT 0 NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_doctor_specialty ON doctors (specialty);
CREATE INDEX idx_doctor_status ON doctors (status);