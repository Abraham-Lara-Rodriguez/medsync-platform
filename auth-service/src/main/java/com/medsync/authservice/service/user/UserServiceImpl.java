package com.medsync.authservice.service.user;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.dto.user.request.UserCreateRequest;
import com.medsync.authservice.dto.user.request.UserFilter;
import com.medsync.authservice.dto.user.request.UserUpdateRequest;
import com.medsync.authservice.dto.user.response.UserResponse;
import com.medsync.authservice.mapper.user.UserMapper;
import com.medsync.authservice.repository.user.UserRepository;
import com.medsync.authservice.repository.user.UserSpecifications;
import com.medsync.commoncore.error.custom.DuplicateResourceException;
import com.medsync.commoncore.error.custom.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> search(UserFilter filter, Pageable pageable) {
        return userRepository.findAll(UserSpecifications.withFilters(filter), pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        User user = findByIdOrThrow(id);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional()
    public UserResponse createUser(UserCreateRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        User user = User.create(request.email(), passwordEncoder.encode(request.password()), request.role());
        return userMapper.toResponse(userRepository.save(user));
    }

    @Override
    @Transactional()
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        return userMapper.toResponse(userRepository.save(updateDataFromDto(findByIdOrThrow(id), request)));
    }

    @Transactional(readOnly = true)
    protected User findByIdOrThrow(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    private User updateDataFromDto(User user, UserUpdateRequest request) {
        if (!user.getEmail().equals(request.email()) && userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        user.changeEmail(request.email());

        if (request.password() != null) {
            user.changePassword(passwordEncoder.encode(request.password()));
        }

        if (!user.getRole().equals(request.role()) && request.role() != null) {
            user.changeRole(request.role());
        }

        return user;
    }
}
