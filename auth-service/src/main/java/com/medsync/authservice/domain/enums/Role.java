package com.medsync.authservice.domain.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum Role {

    ADMIN(Set.of(
            Permission.USER_READ,
            Permission.USER_CREATE,
            Permission.USER_UPDATE,
            Permission.USER_DELETE,

            Permission.PATIENT_CREATE,
            Permission.PATIENT_READ,
            Permission.PATIENT_UPDATE,
            Permission.PATIENT_DELETE,
            Permission.PATIENT_DEACTIVATE,

            Permission.APPOINTMENT_CREATE,
            Permission.APPOINTMENT_READ,
            Permission.APPOINTMENT_UPDATE,
            Permission.APPOINTMENT_CANCEL,

            Permission.DOCTOR_CREATE,
            Permission.DOCTOR_READ,
            Permission.DOCTOR_UPDATE
    )),

    RECEPTIONIST(Set.of(
            Permission.PATIENT_READ,
            Permission.PATIENT_CREATE,
            Permission.PATIENT_UPDATE,

            Permission.APPOINTMENT_READ,
            Permission.APPOINTMENT_CREATE,
            Permission.APPOINTMENT_UPDATE,
            Permission.APPOINTMENT_CANCEL,

            Permission.DOCTOR_READ
    )),

    DOCTOR(Set.of(
            Permission.PATIENT_READ,

            Permission.APPOINTMENT_READ,
            Permission.APPOINTMENT_UPDATE,

            Permission.MEDICAL_RECORD_READ,
            Permission.MEDICAL_RECORD_CREATE,
            Permission.MEDICAL_RECORD_UPDATE
    )),

    PATIENT(Set.of(
            Permission.PATIENT_READ,
            Permission.PATIENT_UPDATE,

            Permission.APPOINTMENT_READ,
            Permission.APPOINTMENT_CREATE,
            Permission.APPOINTMENT_CANCEL
    ));

    //STRUCT TO SECURITY WITH SPRING BOOT SECURITY
    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public String asAuthority() {
        return "ROLE_" + name();
    }

}