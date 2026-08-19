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
    @DisplayName("USER.asAuthority() returns ROLE_USER")
    void userAuthority() {
        assertThat(Role.USER.asAuthority()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("ADMIN has the full set of administrative permissions")
    void adminPermissions() {
        assertThat(Role.ADMIN.getPermissions()).containsExactlyInAnyOrder(
                Permission.ADMIN_CREATE, Permission.ADMIN_READ, Permission.ADMIN_UPDATE, Permission.ADMIN_DELETE);
    }

    @Test
    @DisplayName("USER has the standard user permissions")
    void userPermissions() {
        assertThat(Role.USER.getPermissions()).containsExactlyInAnyOrder(
                Permission.USER_READ, Permission.USER_CREATE, Permission.USER_UPDATE, Permission.USER_DELETE);
    }
}
