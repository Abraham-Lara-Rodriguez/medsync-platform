package com.medsync.patientservice.domain.converter;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HashConfig {

    @Value("${medsync.hash.key:}")
    private String hashKey;

    @PostConstruct
    public void init() {
        if (hashKey == null || hashKey.isBlank()) {
            throw new IllegalStateException("MEDSYNC_HASH_KEY is required to start patient-service");
        }
        DeterministicHasher.initialize(hashKey);
    }
}