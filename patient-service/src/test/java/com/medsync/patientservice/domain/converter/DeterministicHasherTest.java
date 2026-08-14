package com.medsync.patientservice.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicHasherTest {

    @BeforeEach
    void setUp() {
        DeterministicHasher.initialize("test-hash-key-1234567890");
    }

    @Test
    void hashShouldBeDeterministicForSameInput() {
        String first = DeterministicHasher.hash("12345678");
        String second = DeterministicHasher.hash("12345678");

        assertAll(
                () -> assertNotNull(first),
                () -> assertEquals(first, second),
                () -> assertEquals(64, first.length())
        );
    }

    @Test
    void hashShouldReturnNullForBlankInput() {
        assertNull(DeterministicHasher.hash("   "));
        assertNull(DeterministicHasher.hash(null));
    }

    @Test
    void initializeShouldRejectBlankKey() {
        assertThrows(IllegalStateException.class, () -> DeterministicHasher.initialize(" "));
    }
}

