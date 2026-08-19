package com.medsync.authservice.security.userdetails;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl(userRepository);
    }

    @Test
    @DisplayName("returns fully populated UserDetails for an ACTIVE user")
    void returnsUserDetailsForActiveUser() {
        User user = User.create("active@medsync.com", "encoded-pwd", Role.ADMIN);
        when(userRepository.findByEmail("active@medsync.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("active@medsync.com");

        assertThat(details.getUsername()).isEqualTo("active@medsync.com");
        assertThat(details.getPassword()).isEqualTo("encoded-pwd");
        assertThat(details.isEnabled()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorities).contains("ROLE_ADMIN", "ADMIN_CREATE", "ADMIN_READ", "ADMIN_UPDATE", "ADMIN_DELETE");
    }

    @Test
    @DisplayName("returns disabled and locked UserDetails for an INACTIVE user")
    void returnsDisabledUserDetailsForInactiveUser() {
        User user = User.create("inactive@medsync.com", "encoded-pwd", Role.USER);
        user.changeStatus(com.medsync.authservice.domain.enums.UserStatus.INACTIVE);
        when(userRepository.findByEmail("inactive@medsync.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("inactive@medsync.com");

        assertThat(details.isEnabled()).isFalse();
        assertThat(details.isAccountNonLocked()).isFalse();
    }

    @Test
    @DisplayName("maps USER role permissions and authority correctly")
    void mapsUserRolePermissions() {
        User user = User.create("user@medsync.com", "encoded-pwd", Role.USER);
        when(userRepository.findByEmail("user@medsync.com")).thenReturn(Optional.of(user));

        UserDetails details = service.loadUserByUsername("user@medsync.com");

        Set<String> authorities = details.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        assertThat(authorities).containsExactlyInAnyOrder(
                "ROLE_USER", "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE");
    }

    @Test
    @DisplayName("throws UsernameNotFoundException when the user does not exist")
    void throwsWhenUserNotFound() {
        when(userRepository.findByEmail("missing@medsync.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("missing@medsync.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("missing@medsync.com");
    }
}
