package com.medsync.authservice.repository.user;


import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import com.medsync.authservice.dto.user.request.UserFilter;
import com.medsync.authservice.integration.AbstractIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for {@link UserRepository} and {@link UserSpecifications}
 * running against a real PostgreSQL instance (via Testcontainers), including
 * Flyway migrations.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void seed() {
        userRepository.deleteAll();
        userRepository.save(User.create("abraham@medsync.com", "pwd", Role.ADMIN));
        userRepository.save(User.create("sarah@medsync.com", "pwd", Role.USER));
        User inactive = User.create("inactive@medsync.com", "pwd", Role.USER);
        inactive.changeStatus(UserStatus.INACTIVE);
        userRepository.save(inactive);
    }

    @Nested
    @DisplayName("UserRepository")
    class Repository {

        @Test
        @DisplayName("findByEmail returns the user when it exists")
        void findByEmailFound() {
            assertThat(userRepository.findByEmail("abraham@medsync.com")).isPresent();
        }

        @Test
        @DisplayName("findByEmail returns empty when it does not exist")
        void findByEmailNotFound() {
            assertThat(userRepository.findByEmail("nobody@medsync.com")).isEmpty();
        }

        @Test
        @DisplayName("existsByEmail returns true/false correctly")
        void existsByEmail() {
            assertThat(userRepository.existsByEmail("abraham@medsync.com")).isTrue();
            assertThat(userRepository.existsByEmail("nobody@medsync.com")).isFalse();
        }
    }

    @Nested
    @DisplayName("UserSpecifications")
    class Specifications {

        @Test
        @DisplayName("returns all users when the filter is empty")
        void emptyFilterReturnsAll() {
            UserFilter filter = new UserFilter(null, null, null);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).hasSize(3);
        }

        @Test
        @DisplayName("filters by case-insensitive partial email match")
        void filtersBySearchText() {
            UserFilter filter = new UserFilter("ABRA", null, null);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).extracting(User::getEmail).containsExactly("abraham@medsync.com");
        }

        @Test
        @DisplayName("filters by role")
        void filtersByRole() {
            UserFilter filter = new UserFilter(null, Role.ADMIN, null);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).extracting(User::getEmail).containsExactly("abraham@medsync.com");
        }

        @Test
        @DisplayName("filters by status")
        void filtersByStatus() {
            UserFilter filter = new UserFilter(null, null, UserStatus.INACTIVE);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).extracting(User::getEmail).containsExactly("inactive@medsync.com");
        }

        @Test
        @DisplayName("combines multiple filters with AND semantics")
        void combinesFiltersWithAnd() {
            UserFilter filter = new UserFilter("sarah", Role.USER, UserStatus.ACTIVE);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).extracting(User::getEmail).containsExactly("sarah@medsync.com");
        }

        @Test
        @DisplayName("returns empty when filters do not match any user")
        void returnsEmptyWhenNoMatch() {
            UserFilter filter = new UserFilter("sarah", Role.ADMIN, null);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @DisplayName("blank search text is ignored")
        void blankSearchIsIgnored() {
            UserFilter filter = new UserFilter("   ", null, null);

            Page<User> result = userRepository.findAll(UserSpecifications.withFilters(filter), Pageable.unpaged());

            assertThat(result.getContent()).hasSize(3);
        }
    }
}
