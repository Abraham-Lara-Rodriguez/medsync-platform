package com.medsync.patientservice.domain.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
    @DisplayName("Should convert To Database Column should Encrypt Value")
    void convertToDatabaseColumn_shouldEncryptValue() {
        String original = "123456789";
        String encrypted = converter.convertToDatabaseColumn(original);
        assertAll(
                () -> assertNotEquals(original, encrypted),
                () -> assertNotNull(encrypted)
        );
    }

    @Test
    @DisplayName("Should convert To Database Column should Return Null When Input Is Blank")
    void convertToDatabaseColumn_shouldReturnNullWhenInputIsBlank() {
        assertNull(converter.convertToDatabaseColumn("   "));
    }

    @Test
    @DisplayName("Should convert To Database Column should Return Blank And Null As Is")
    void convertToDatabaseColumn_shouldReturnBlankAndNullAsIs() {
        assertEquals("", converter.convertToDatabaseColumn(""));
        assertNull(converter.convertToDatabaseColumn(null));
    }

    @Test
    @DisplayName("Should convert To Entity Attribute should Decrypt Value")
    void convertToEntityAttribute_shouldDecryptValue() {
        String original = converter.convertToDatabaseColumn("123456789");
        String decrypted = converter.convertToEntityAttribute(original);
        assertAll(
                () -> assertEquals("123456789", decrypted),
                () -> assertNotNull(decrypted)
        );
    }

    @Test
    @DisplayName("Should convert To Entity Attribute should Return Blank And Null As Is")
    void convertToEntityAttribute_shouldReturnBlankAndNullAsIs() {
        assertEquals("", converter.convertToEntityAttribute(""));
        assertNull(converter.convertToEntityAttribute(null));
    }

    @Test
    @DisplayName("Should convert To Entity Attribute should Return Null When Input Is Blank")
    void convertToEntityAttribute_shouldReturnNullWhenInputIsBlank() {
        assertNull(converter.convertToEntityAttribute("   "));
    }

    @Test
    @DisplayName("Should initialize should Reject Blank Key")
    void initialize_shouldRejectBlankKey() {
        assertThrows(IllegalStateException.class, () -> EncryptedStringConverter.initialize(" "));
    }

    @Test
    @DisplayName("Should convert To Database Column should Fail When Not Initialized")
    void convertToDatabaseColumn_shouldFailWhenNotInitialized() throws Exception {
        clearEncryptor();

        assertThrows(IllegalStateException.class, () -> converter.convertToDatabaseColumn("123456789"));
    }

    @Test
    @DisplayName("Should convert To Entity Attribute should Fail When Not Initialized")
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
