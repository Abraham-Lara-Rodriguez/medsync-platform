package com.medsync.authservice.service.user;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.domain.enums.UserStatus;
import com.medsync.authservice.dto.user.request.CreateUserRequest;
import com.medsync.authservice.dto.user.request.UpdateUserRequest;
import com.medsync.authservice.dto.user.request.UserFilter;
import com.medsync.authservice.dto.user.response.UserResponse;
import com.medsync.authservice.mapper.user.UserMapper;
import com.medsync.authservice.repository.user.UserRepository;
import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);
    }

    private static User existingUser(String email, Role role) {
        return User.create(email, "encoded-password", role);
    }

    @Nested
    @DisplayName("getAllUsers")
    class GetAllUsers {

        @Test
        @DisplayName("maps the repository page through the mapper")
        void mapsRepositoryPage() {
            Pageable pageable = Pageable.unpaged();
            User user = existingUser("a@medsync.com", Role.USER);
            UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());

            when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user)));
            when(userMapper.toResponse(user)).thenReturn(response);

            Page<UserResponse> result = userService.getAllUsers(pageable);

            assertThat(result.getContent()).containsExactly(response);
        }
    }

    @Nested
    @DisplayName("search")
    class Search {

        @Test
        @DisplayName("builds a specification from the filter and maps the results")
        void buildsSpecificationAndMaps() {
            Pageable pageable = Pageable.unpaged();
            UserFilter filter = new UserFilter("abra", Role.ADMIN, UserStatus.ACTIVE);
            User user = existingUser("abraham@medsync.com", Role.ADMIN);
            UserResponse response = new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus());

            when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(List.of(user)));
            when(userMapper.toResponse(user)).thenReturn(response);

            Page<UserResponse> result = userService.search(filter, pageable);

            assertThat(result.getContent()).containsExactly(response);
            verify(userRepository).findAll(any(Specification.class), eq(pageable));
        }
    }

    @Nested
    @DisplayName("getUserById")
    class GetUserById {

        @Test
        @DisplayName("returns the mapped user when found")
        void returnsUserWhenFound() {
            UUID id = UUID.randomUUID();
            User user = existingUser("a@medsync.com", Role.USER);
            UserResponse response = new UserResponse(id, user.getEmail(), user.getRole(), user.getStatus());

            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userMapper.toResponse(user)).thenReturn(response);

            UserResponse result = userService.getUserById(id);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when not found")
        void throwsWhenNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(id))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }
    }

    @Nested
    @DisplayName("createUser")
    class CreateUser {

        @Test
        @DisplayName("creates and saves the user when the email is not taken")
        void createsUserWhenEmailAvailable() {
            CreateUserRequest request = new CreateUserRequest("new@medsync.com", "raw-password", Role.USER);
            User savedUser = existingUser("new@medsync.com", Role.USER);
            UserResponse response = new UserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole(), savedUser.getStatus());

            when(userRepository.existsByEmail("new@medsync.com")).thenReturn(false);
            when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponse(savedUser)).thenReturn(response);

            UserResponse result = userService.createUser(request);

            assertThat(result).isEqualTo(response);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo("new@medsync.com");
            assertThat(captor.getValue().getPassword()).isEqualTo("encoded-password");
            assertThat(captor.getValue().getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("throws DuplicateResourceException and never saves when email already exists")
        void throwsWhenEmailAlreadyExists() {
            CreateUserRequest request = new CreateUserRequest("dup@medsync.com", "raw-password", Role.USER);
            when(userRepository.existsByEmail("dup@medsync.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("dup@medsync.com");

            verify(userRepository, never()).save(any());
            verifyNoInteractions(passwordEncoder);
        }
    }

    @Nested
    @DisplayName("updateUser")
    class UpdateUser {

        @Test
        @DisplayName("throws ResourceNotFoundException when the user does not exist")
        void throwsWhenUserNotFound() {
            UUID id = UUID.randomUUID();
            when(userRepository.findById(id)).thenReturn(Optional.empty());

            UpdateUserRequest request = new UpdateUserRequest("a@medsync.com", null, null);

            assertThatThrownBy(() -> userService.updateUser(id, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws DuplicateResourceException when changing to an email already used by another user")
        void throwsWhenNewEmailAlreadyExists() {
            UUID id = UUID.randomUUID();
            User user = existingUser("old@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("taken@medsync.com")).thenReturn(true);

            UpdateUserRequest request = new UpdateUserRequest("taken@medsync.com", null, null);

            assertThatThrownBy(() -> userService.updateUser(id, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("taken@medsync.com");

            verify(userRepository, never()).save(any());
        }

        @Test
        @DisplayName("does not check email availability when the email is unchanged")
        void doesNotCheckAvailabilityWhenEmailUnchanged() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(
                    new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus()));

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", null, null);

            userService.updateUser(id, request);

            verify(userRepository, never()).existsByEmail(anyString());
        }

        @Test
        @DisplayName("updates the email when it changes and is available")
        void updatesEmailWhenAvailable() {
            UUID id = UUID.randomUUID();
            User user = existingUser("old@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.existsByEmail("new@medsync.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getStatus());
            });

            UpdateUserRequest request = new UpdateUserRequest("new@medsync.com", null, null);

            UserResponse result = userService.updateUser(id, request);

            assertThat(result.email()).isEqualTo("new@medsync.com");
        }

        @Test
        @DisplayName("encodes and updates the password when provided")
        void updatesPasswordWhenProvided() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.encode("new-raw-password")).thenReturn("new-encoded-password");
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(
                    new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus()));

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", "new-raw-password", null);

            userService.updateUser(id, request);

            ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(captor.capture());
            assertThat(captor.getValue().getPassword()).isEqualTo("new-encoded-password");
        }

        @Test
        @DisplayName("does not touch the password when it is null")
        void doesNotChangePasswordWhenNull() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            String originalPassword = user.getPassword();
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenReturn(
                    new UserResponse(user.getId(), user.getEmail(), user.getRole(), user.getStatus()));

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", null, null);

            userService.updateUser(id, request);

            assertThat(user.getPassword()).isEqualTo(originalPassword);
            verifyNoInteractions(passwordEncoder);
        }

        @Test
        @DisplayName("updates the role when it changes and is not null")
        void updatesRoleWhenChanged() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getStatus());
            });

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", null, Role.ADMIN);

            UserResponse result = userService.updateUser(id, request);

            assertThat(result.role()).isEqualTo(Role.ADMIN);
        }

        @Test
        @DisplayName("keeps the role unchanged when the request role is null")
        void keepsRoleWhenRequestRoleIsNull() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getStatus());
            });

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", null, null);

            UserResponse result = userService.updateUser(id, request);

            assertThat(result.role()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("keeps the role unchanged when the request role equals the current role")
        void keepsRoleWhenRequestRoleEqualsCurrent() {
            UUID id = UUID.randomUUID();
            User user = existingUser("same@medsync.com", Role.USER);
            when(userRepository.findById(id)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
            when(userMapper.toResponse(any(User.class))).thenAnswer(inv -> {
                User u = inv.getArgument(0);
                return new UserResponse(u.getId(), u.getEmail(), u.getRole(), u.getStatus());
            });

            UpdateUserRequest request = new UpdateUserRequest("same@medsync.com", null, Role.USER);

            UserResponse result = userService.updateUser(id, request);

            assertThat(result.role()).isEqualTo(Role.USER);
        }
    }
}
