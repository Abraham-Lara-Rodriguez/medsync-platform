package com.medsync.authservice.service.user;

import com.medsync.authservice.dto.user.request.UserCreateRequest;
import com.medsync.authservice.dto.user.request.UserFilter;
import com.medsync.authservice.dto.user.request.UserUpdateRequest;
import com.medsync.authservice.dto.user.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);

    Page<UserResponse> search(UserFilter filter, Pageable pageable);

    UserResponse getUserById(UUID id);

    UserResponse createUser(UserCreateRequest request);

    UserResponse updateUser(UUID id, UserUpdateRequest request);
}
