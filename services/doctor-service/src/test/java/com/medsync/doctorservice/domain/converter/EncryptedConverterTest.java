package com.medsync.doctorservice.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedConverterTest {

    private static final String VALID_KEY = "test-encryption-key-for-jwt-tests";

    private final EncryptedConverter converter = new EncryptedConverter();

    @BeforeEach
    void setUp() {
        EncryptedConverter.initialize(VALID_KEY);
    }

    @Test
    void initialize_shouldThrowWhenKeyIsNull() {
        assertThrows(IllegalStateException.class, () -> EncryptedConverter.initialize(null));
    }

    @Test
    void initialize_shouldThrowWhenKeyIsBlank() {
        assertThrows(IllegalStateException.class, () -> EncryptedConverter.initialize("   "));
    }

    @Test
    void initialize_shouldSetEncryptor() {
        EncryptedConverter.initialize("another-valid-key");
        String encrypted = converter.convertToDatabaseColumn("test");
        assertNotNull(encrypted);
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNullWhenAttributeIsNull() {
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnEmptyWhenAttributeIsEmpty() {
        assertEquals("", converter.convertToDatabaseColumn(""));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNullWhenAttributeIsBlank() {
        assertNull(converter.convertToDatabaseColumn("   "));
    }

    @Test
    void convertToDatabaseColumn_shouldEncryptNonEmptyString() {
        String encrypted = converter.convertToDatabaseColumn("test-value");
        assertNotNull(encrypted);
        assertNotEquals("test-value", encrypted);
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullWhenDbDataIsNull() {
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldReturnEmptyWhenDbDataIsEmpty() {
        assertEquals("", converter.convertToEntityAttribute(""));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullWhenDbDataIsBlank() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void roundTrip_shouldEncryptAndDecryptCorrectly() {
        String original = "sensitive-data";
        String encrypted = converter.convertToDatabaseColumn(original);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals(original, decrypted);
    }
}