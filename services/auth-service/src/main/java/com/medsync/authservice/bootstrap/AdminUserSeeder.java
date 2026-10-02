package com.medsync.authservice.bootstrap;

import com.medsync.authservice.domain.entity.User;
import com.medsync.authservice.domain.enums.Role;
import com.medsync.authservice.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.enabled:false}")
    private boolean enabled;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        if (!enabled) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.info("Bootstrap admin already exists. Skipping.");
            return;
        }

        User admin = User.create(
                email,
                passwordEncoder.encode(password),
                Role.ADMIN
        );

        userRepository.save(admin);

        log.info("Bootstrap admin created successfully: {}", email);
    }
}