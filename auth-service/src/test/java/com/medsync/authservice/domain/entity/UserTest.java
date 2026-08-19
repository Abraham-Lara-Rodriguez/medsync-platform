package com.medsync.authservice.domain.entity;

import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    @DisplayName("create() builds a user with ACTIVE status and given attributes")
    void createBuildsActiveUser() {
        User user = User.create("new@medsync.com", "encoded-password", Role.USER);

        assertThat(user.getEmail()).isEqualTo("new@medsync.com");
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("changeEmail mutates the email")
    void changeEmailMutatesState() {
        User user = User.create("old@medsync.com", "pwd", Role.USER);

        user.changeEmail("new@medsync.com");

        assertThat(user.getEmail()).isEqualTo("new@medsync.com");
    }

    @Test
    @DisplayName("changePassword mutates the password")
    void changePasswordMutatesState() {
        User user = User.create("a@medsync.com", "old-encoded", Role.USER);

        user.changePassword("new-encoded");

        assertThat(user.getPassword()).isEqualTo("new-encoded");
    }

    @Test
    @DisplayName("changeRole mutates the role")
    void changeRoleMutatesState() {
        User user = User.create("a@medsync.com", "pwd", Role.USER);

        user.changeRole(Role.ADMIN);

        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("changeStatus mutates the status")
    void changeStatusMutatesState() {
        User user = User.create("a@medsync.com", "pwd", Role.USER);

        user.changeStatus(UserStatus.INACTIVE);

        assertThat(user.getStatus()).isEqualTo(UserStatus.INACTIVE);
    }
}
