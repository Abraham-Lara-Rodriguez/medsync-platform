package com.medsync.authservice.service.user;

import com.medsync.authservice.dto.user.request.CreateUserRequest;
import com.medsync.authservice.dto.user.request.UpdateUserRequest;
import com.medsync.authservice.dto.user.request.UserFilter;
import com.medsync.authservice.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> search(UserFilter filter, Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUser(UUID id, UpdateUserRequest request);
}
