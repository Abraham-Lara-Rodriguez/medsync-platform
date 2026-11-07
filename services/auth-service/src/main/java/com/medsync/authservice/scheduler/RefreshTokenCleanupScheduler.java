package com.medsync.authservice.scheduler;

import com.medsync.authservice.repository.auth.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "America/Bogota")
    @Transactional
    public void purgeExpiredTokens() {
        long deleted = refreshTokenRepository.deleteByExpiresAtBefore(Instant.now());
        log.info("Deleted {} expired refresh tokens", deleted);
    }
}