package com.medsync.authservice.user.service;


import com.medsync.authservice.mapper.user.UserMapper;
import com.medsync.authservice.repository.user.UserRepository;
import com.medsync.authservice.service.user.UserServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

class UserServiceImplTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final UserMapper userMapper = mock(UserMapper.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final UserServiceImpl userService = new UserServiceImpl(userRepository, userMapper, passwordEncoder);


}