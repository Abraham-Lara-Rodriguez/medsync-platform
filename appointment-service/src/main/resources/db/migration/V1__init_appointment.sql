CREATE TABLE appointment
(
    id               uuid         NOT NULL,
    patient_id       uuid         NOT NULL,
    doctor_id        uuid         NOT NULL,
    reason           varchar(500),
    notes            varchar(2000),
    type             varchar(255) NOT NULL,
    appointment_date date         NOT NULL,
    status           varchar(255) NOT NULL,
    start_time       time(0)      NOT NULL,
    end_time         time(0)      NOT NULL,
    created_at       timestamptz(6)   NOT NULL,
    updated_at       timestamptz(6)   NOT NULL,
    version          integer      NOT NULL
);

ALTER TABLE appointment
    ADD CONSTRAINT appointment_pkey PRIMARY KEY (id);

CREATE INDEX idx_appointment_patient
    ON appointment(patient_id);

CREATE INDEX idx_appointment_doctor_date
    ON appointment(doctor_id, appointment_date);

CREATE INDEX idx_appointment_status
    ON appointment(status);

CREATE INDEX idx_appointment_doctor_date_time
    ON appointment(doctor_id, appointment_date, start_time);

ALTER TABLE appointment
    ADD CONSTRAINT chk_appointment_time
        CHECK (start_time < end_time);