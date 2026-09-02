package com.medsync.authservice.domain.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    @DisplayName("ADMIN.asAuthority() returns ROLE_ADMIN")
    void adminAuthority() {
        assertThat(Role.ADMIN.asAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("RECEPTIONIST.asAuthority() returns ROLE_RECEPTIONIST")
    void userAuthority() {
        assertThat(Role.RECEPTIONIST.asAuthority()).isEqualTo("ROLE_RECEPTIONIST");
    }

    @Test
    @DisplayName("ADMIN has the full set of administrative permissions")
    void adminPermissions() {
        assertThat(Role.ADMIN.getPermissions()).containsExactlyInAnyOrder(
                Permission.USER_READ, Permission.USER_CREATE,
                Permission.USER_UPDATE, Permission.USER_DELETE,

                Permission.PATIENT_CREATE, Permission.PATIENT_READ,
                Permission.PATIENT_UPDATE, Permission.PATIENT_DELETE,
                Permission.PATIENT_DEACTIVATE,

                Permission.APPOINTMENT_CREATE, Permission.APPOINTMENT_READ,
                Permission.APPOINTMENT_UPDATE, Permission.APPOINTMENT_CANCEL,

                Permission.DOCTOR_CREATE, Permission.DOCTOR_READ,
                Permission.DOCTOR_UPDATE);
    }

    @Test
    @DisplayName("RECEPTIONIST has the standard user permissions")
    void userPermissions() {
        assertThat(Role.RECEPTIONIST.getPermissions()).containsExactlyInAnyOrder(
                Permission.PATIENT_READ,
                Permission.PATIENT_CREATE,
                Permission.PATIENT_UPDATE,

                Permission.APPOINTMENT_READ,
                Permission.APPOINTMENT_CREATE,
                Permission.APPOINTMENT_UPDATE,
                Permission.APPOINTMENT_CANCEL,

                Permission.DOCTOR_READ
        );
    }
}
