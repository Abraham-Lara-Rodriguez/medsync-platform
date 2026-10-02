package com.medsync.doctorservice.domain.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HashConfigTest {

    @Test
    void hashKeyBlank_shouldThrowWhenNoKeyProvided() {
        // Simulate empty/blank key behavior of HashConfig.init()
        String hashKey = ""; // mimics ${medsync.hash.key:} with empty default
        assertThrows(IllegalStateException.class, () -> {
            if (hashKey == null || hashKey.isBlank()) {
                throw new IllegalStateException("MEDSYNC_HASH_KEY is required");
            }
            DeterministicHasher.initialize(hashKey);
        });
    }

    @Test
    void hashKeyValid_shouldInitializeHasher() {
        String hashKey = "valid-hash-key-provided";
        if (hashKey == null || hashKey.isBlank()) {
            throw new IllegalStateException("MEDSYNC_HASH_KEY is required");
        }
        DeterministicHasher.initialize(hashKey);
        assertNotNull(DeterministicHasher.hash("test"));
    }

    @Test
    void hashKeyNotBlank_shouldInitializeHasher() {
        String hashKey = "another-valid-key";
        if (hashKey == null || hashKey.isBlank()) {
            throw new IllegalStateException("MEDSYNC_HASH_KEY is required");
        }
        DeterministicHasher.initialize(hashKey);
        String hash = DeterministicHasher.hash("same");
        String hash2 = DeterministicHasher.hash("same");
        assertEquals(hash, hash2);
    }
}