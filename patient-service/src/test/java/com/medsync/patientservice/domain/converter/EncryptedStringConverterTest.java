package com.medsync.patientservice.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class EncryptedStringConverterTest {

    private final EncryptedStringConverter converter = new EncryptedStringConverter();

    @BeforeEach
    void setUp() {
        EncryptedStringConverter.initialize("test-encryption-key-1234567890");
    }

    @Test
    void convertToDatabaseColumn_shouldEncryptValue() {
        String original = "123456789";
        String encrypted = converter.convertToDatabaseColumn(original);
        assertAll(
                () -> assertNotEquals(original, encrypted),
                () -> assertNotNull(encrypted)
        );
    }

    @Test
    void convertToDatabaseColumn_shouldReturnNullWhenInputIsBlank() {
        assertNull(converter.convertToDatabaseColumn("   "));
    }

    @Test
    void convertToDatabaseColumn_shouldReturnBlankAndNullAsIs() {
        assertEquals("", converter.convertToDatabaseColumn(""));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    void convertToEntityAttribute_shouldDecryptValue() {
        String original = converter.convertToDatabaseColumn("123456789");
        String decrypted = converter.convertToEntityAttribute(original);
        assertAll(
                () -> assertEquals("123456789", decrypted),
                () -> assertNotNull(decrypted)
        );
    }

    @Test
    void convertToEntityAttribute_shouldReturnBlankAndNullAsIs() {
        assertEquals("", converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    void convertToEntityAttribute_shouldReturnNullWhenInputIsBlank() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    void initialize_shouldRejectBlankKey() {
        assertThrows(IllegalStateException.class, () -> EncryptedStringConverter.initialize(" "));
    }

    @Test
    void convertToDatabaseColumn_shouldFailWhenNotInitialized() throws Exception {
        clearEncryptor();

        assertThrows(IllegalStateException.class, () -> converter.convertToDatabaseColumn("123456789"));
    }

    @Test
    void convertToEntityAttribute_shouldFailWhenNotInitialized() throws Exception {
        clearEncryptor();

        assertThrows(IllegalStateException.class, () -> converter.convertToEntityAttribute("encrypted-value"));
    }

    private void clearEncryptor() throws Exception {
        Field field = EncryptedStringConverter.class.getDeclaredField("ENCRYPTOR");
        field.setAccessible(true);
        field.set(null, null);
    }
}