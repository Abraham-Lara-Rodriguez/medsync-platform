package com.medsync.patientservice.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class DeterministicHasherTest {

    @BeforeEach
    void setUp() {
        DeterministicHasher.initialize("test-hash-key-1234567890");
    }

    @Test
    @DisplayName("Should hash Should Be Deterministic And64 Hex Characters")
    void hashShouldBeDeterministicAnd64HexCharacters() {
        String first = DeterministicHasher.hash("12345678");
        String second = DeterministicHasher.hash("12345678");

        assertAll(
                () -> assertNotNull(first),
                () -> assertEquals(first, second),
                () -> assertEquals(64, first.length()),
                () -> assertTrue(first.matches("[0-9a-f]{64}"))
        );
    }

    @Test
    @DisplayName("Should hash Should Return Different Value For Different Input")
    void hashShouldReturnDifferentValueForDifferentInput() {
        assertNotEquals(
                DeterministicHasher.hash("12345678"),
                DeterministicHasher.hash("87654321")
        );
    }

    @Test
    @DisplayName("Should hash Should Return Null For Null Or Blank Input")
    void hashShouldReturnNullForNullOrBlankInput() {
        assertAll(
                () -> assertNull(DeterministicHasher.hash(null)),
                () -> assertNull(DeterministicHasher.hash("")),
                () -> assertNull(DeterministicHasher.hash("   "))
        );
    }

    @Test
    @DisplayName("Should initialize Should Reject Null Or Blank Secret")
    void initializeShouldRejectNullOrBlankSecret() {
        assertAll(
                () -> assertThrows(IllegalStateException.class, () -> DeterministicHasher.initialize(null)),
                () -> assertThrows(IllegalStateException.class, () -> DeterministicHasher.initialize("")),
                () -> assertThrows(IllegalStateException.class, () -> DeterministicHasher.initialize("   "))
        );
    }

    @Test
    @DisplayName("Should hash Should Fail Before Initialization")
    void hashShouldFailBeforeInitialization() throws Exception {
        Field key = DeterministicHasher.class.getDeclaredField("KEY");
        key.setAccessible(true);
        key.set(null, null);

        assertThrows(
                IllegalStateException.class,
                () -> DeterministicHasher.hash("12345678")
        );
    }
}
