package com.medsync.authservice.domain.enums;

public enum Permission {
    // User management
    USER_READ,
    USER_CREATE,
    USER_UPDATE,
    USER_DELETE,

    // Patient management
    PATIENT_READ,
    PATIENT_CREATE,
    PATIENT_UPDATE,
    PATIENT_DELETE,
    PATIENT_DEACTIVATE,

    // Appointment management
    APPOINTMENT_READ,
    APPOINTMENT_CREATE,
    APPOINTMENT_UPDATE,
    APPOINTMENT_CANCEL,

    // Doctor management
    DOCTOR_CREATE,
    DOCTOR_READ,
    DOCTOR_UPDATE,

    // Medical records
    MEDICAL_RECORD_CREATE,
    MEDICAL_RECORD_READ,
    MEDICAL_RECORD_UPDATE
}
