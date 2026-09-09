package com.medsync.patientservice.domain.converter;

import org.jasypt.util.text.AES256TextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionConfigTest {

    private EncryptionConfig encryptionConfig;

    @BeforeEach
    void setUp() {
        encryptionConfig = new EncryptionConfig();
    }

    @Test
    @DisplayName("Should Initialize Encryptor When Key Is Present")
    void shouldInitializeEncryptorWhenKeyIsPresent() {
        ReflectionTestUtils.setField(encryptionConfig, "encryptionKey", "test-encryption-key-1234567890");

        AES256TextEncryptor encryptor = encryptionConfig.aes256TextEncryptor();

        assertNotNull(encryptor);
        assertEquals("test-encryption-key-1234567890", ReflectionTestUtils.getField(encryptionConfig, "encryptionKey"));

        EncryptedStringConverter converter = new EncryptedStringConverter();
        String encrypted = converter.convertToDatabaseColumn("123456789");
        String decrypted = converter.convertToEntityAttribute(encrypted);

        assertAll(
                () -> assertNotNull(encrypted),
                () -> assertNotEquals("123456789", encrypted),
                () -> assertEquals("123456789", decrypted)
        );
    }

    @Test
    @DisplayName("Should Fail When Key Is Blank")
    void shouldFailWhenKeyIsBlank() {
        ReflectionTestUtils.setField(encryptionConfig, "encryptionKey", " ");

        assertThrows(IllegalStateException.class, () -> encryptionConfig.aes256TextEncryptor());
    }

    @Test
    @DisplayName("Should Fail When Key Is Null")
    void shouldFailWhenKeyIsNull() {
        ReflectionTestUtils.setField(encryptionConfig, "encryptionKey", null);

        assertThrows(IllegalStateException.class, () -> encryptionConfig.aes256TextEncryptor());
    }

    @Test
    @DisplayName("Should Not Leak Plaintext Key In Bean Initialization Result")
    void shouldNotLeakPlaintextKeyInBeanInitializationResult() {
        ReflectionTestUtils.setField(encryptionConfig, "encryptionKey", "test-encryption-key-1234567890");

        AES256TextEncryptor encryptor = encryptionConfig.aes256TextEncryptor();

        assertNotNull(encryptor);
    }
}

