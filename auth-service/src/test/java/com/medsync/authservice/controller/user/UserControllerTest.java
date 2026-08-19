package com.medsync.authservice.controller.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import com.medsync.authservice.dto.user.request.UserCreateRequest;
import com.medsync.authservice.dto.user.request.UserUpdateRequest;
import com.medsync.authservice.dto.user.response.UserResponse;
import com.medsync.authservice.service.user.UserService;
import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer tests for {@link UserController}. Security ({@code @PreAuthorize})
 * is disabled here to isolate request mapping, validation and serialization;
 * authorization rules are covered by the integration tests.
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private static UserResponse sampleResponse(UUID id) {
        return new UserResponse(id, "user@medsync.com", Role.USER, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("GET /api/v1/users returns 200 with a page of users")
    void getAllUsersReturnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.getAllUsers(any())).thenReturn(new PageImpl<>(List.of(sampleResponse(id))));

        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("user@medsync.com"));
    }

    @Test
    @DisplayName("GET /api/v1/users/search returns 200 with filtered results")
    void searchReturnsFilteredPage() throws Exception {
        UUID id = UUID.randomUUID();
        when(userService.search(any(), any())).thenReturn(new PageImpl<>(List.of(sampleResponse(id))));

        mockMvc.perform(get("/api/v1/users/search").param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("USER"));
    }

    @Nested
    class GetUserById {

        @Test
        @DisplayName("GET /api/v1/users/{id} returns 200 when found")
        void returnsUserWhenFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.getUserById(id)).thenReturn(sampleResponse(id));

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("user@medsync.com"));
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} returns 404 when not found")
        void returns404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.getUserById(id)).thenThrow(new ResourceNotFoundException("User not found with id: " + id));

            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("GET /api/v1/users/{id} returns 400 when id is not a valid UUID")
        void returns400WhenIdIsMalformed() throws Exception {
            mockMvc.perform(get("/api/v1/users/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class CreateUser {

        @Test
        @DisplayName("POST /api/v1/users returns 201 with a Location header")
        void createsUser() throws Exception {
            UUID id = UUID.randomUUID();
            UserCreateRequest request = new UserCreateRequest("new@medsync.com", "raw-password", Role.USER);
            when(userService.createUser(any(UserCreateRequest.class))).thenReturn(sampleResponse(id));

            mockMvc.perform(post("/api/v1/users")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", "/api/v1/users/" + id));
        }

        @Test
        @DisplayName("POST /api/v1/users returns 400 when email is invalid")
        void rejectsInvalidEmail() throws Exception {
            UserCreateRequest request = new UserCreateRequest("not-an-email", "raw-password", Role.USER);

            mockMvc.perform(post("/api/v1/users")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/users returns 400 when role is missing")
        void rejectsMissingRole() throws Exception {
            UserCreateRequest request = new UserCreateRequest("new@medsync.com", "raw-password", null);

            mockMvc.perform(post("/api/v1/users")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /api/v1/users returns 409 when email already exists")
        void returns409WhenEmailExists() throws Exception {
            UserCreateRequest request = new UserCreateRequest("dup@medsync.com", "raw-password", Role.USER);
            when(userService.createUser(any(UserCreateRequest.class)))
                    .thenThrow(new DuplicateResourceException("Email already exists: dup@medsync.com"));

            mockMvc.perform(post("/api/v1/users")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class UpdateUser {

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 200 with updated user")
        void updatesUser() throws Exception {
            UUID id = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("updated@medsync.com", null, Role.ADMIN);
            when(userService.updateUser(eq(id), any(UserUpdateRequest.class)))
                    .thenReturn(new UserResponse(id, "updated@medsync.com", Role.ADMIN, UserStatus.ACTIVE));

            mockMvc.perform(put("/api/v1/users/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("updated@medsync.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 404 when the user does not exist")
        void returns404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("updated@medsync.com", null, null);
            when(userService.updateUser(eq(id), any(UserUpdateRequest.class)))
                    .thenThrow(new ResourceNotFoundException("User not found with id: " + id));

            mockMvc.perform(put("/api/v1/users/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("PUT /api/v1/users/{id} returns 400 when email is malformed")
        void rejectsInvalidEmail() throws Exception {
            UUID id = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("not-an-email", null, null);

            mockMvc.perform(put("/api/v1/users/{id}", id)
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
