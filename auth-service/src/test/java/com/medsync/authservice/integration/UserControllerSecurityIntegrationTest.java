package com.medsync.authservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.dto.user.request.CreateUserRequest;
import com.medsync.authservice.help.AuthTestHelper;
import com.medsync.authservice.repository.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration test verifying that method-level security ({@code @PreAuthorize})
 * on {@link com.medsync.authservice.controller.user.UserController} is enforced
 * end-to-end through the real Spring Security configuration and JWT authentication.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerSecurityIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@medsync.com";
    private static final String USER_EMAIL = "user@medsync.com";
    private static final String PASSWORD = "Sup3rSecret!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seed() {
        userRepository.deleteAll();

        User admin = User.create(
                ADMIN_EMAIL,
                passwordEncoder.encode(PASSWORD),
                Role.ADMIN
        );

        User user = User.create(
                USER_EMAIL,
                passwordEncoder.encode(PASSWORD),
                Role.USER
        );

        userRepository.save(admin);
        userRepository.save(user);
    }

    @Test
    @DisplayName("GET /api/v1/users with ADMIN_READ authority returns 200")
    void listUsersAllowedWithAdminRead() throws Exception {

        String accessToken = AuthTestHelper.obtainAccessToken(
                mockMvc,
                objectMapper,
                ADMIN_EMAIL,
                PASSWORD
        );

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/users without ADMIN_READ authority returns 403")
    void listUsersForbiddenWithoutAdminRead() throws Exception {

        String accessToken = AuthTestHelper.obtainAccessToken(
                mockMvc,
                objectMapper,
                USER_EMAIL,
                PASSWORD
        );

        mockMvc.perform(get("/api/v1/users").header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users/{id} without ADMIN_READ authority returns 403")
    void getUserByIdForbiddenWithoutAdminRead() throws Exception {

        String accessToken = AuthTestHelper.obtainAccessToken(
                mockMvc,
                objectMapper,
                USER_EMAIL,
                PASSWORD
        );

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID()).header("Authorization", "Bearer " + accessToken)
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/users with ADMIN_CREATE authority returns 201")
    void createUserAllowedWithAdminCreate() throws Exception {

        String accessToken = AuthTestHelper.obtainAccessToken(
                mockMvc,
                objectMapper,
                ADMIN_EMAIL,
                PASSWORD
        );

        CreateUserRequest request = new CreateUserRequest(
                "brand-new@medsync.com",
                "raw-password",
                Role.USER
        );

        mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/users without ADMIN_CREATE authority returns 403")
    void createUserForbiddenWithoutAdminCreate() throws Exception {

        String accessToken = AuthTestHelper.obtainAccessToken(
                mockMvc,
                objectMapper,
                USER_EMAIL,
                PASSWORD
        );

        CreateUserRequest request = new CreateUserRequest(
                "blocked@medsync.com",
                "raw-password",
                Role.USER
        );

        mockMvc.perform(
                post("/api/v1/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request))
        ).andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/users without authentication returns 401")
    void listUsersUnauthenticatedReturns401() throws Exception {

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());
    }
}