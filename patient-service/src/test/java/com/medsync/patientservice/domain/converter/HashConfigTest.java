package com.medsync.patientservice.domain.converter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class HashConfigTest {

    @Test
    @DisplayName("Should init Should Initialize Hasher When Key Is Valid")
    void initShouldInitializeHasherWhenKeyIsValid() {
        HashConfig config = new HashConfig();
        ReflectionTestUtils.setField(config, "hashKey", "test-hash-key-1234567890");

        assertDoesNotThrow(config::init);
        assertNotNull(DeterministicHasher.hash("12345678"));
    }

    @Test
    @DisplayName("Should init Should Reject Null Key")
    void initShouldRejectNullKey() {
        HashConfig config = new HashConfig();
        ReflectionTestUtils.setField(config, "hashKey", null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, config::init);

        assertEquals("MEDSYNC_HASH_KEY is required to start patient-service", ex.getMessage());
    }

    @Test
    @DisplayName("Should init Should Reject Blank Key")
    void initShouldRejectBlankKey() {
        HashConfig config = new HashConfig();
        ReflectionTestUtils.setField(config, "hashKey", " ");

        assertThrows(IllegalStateException.class, config::init);
    }
}
