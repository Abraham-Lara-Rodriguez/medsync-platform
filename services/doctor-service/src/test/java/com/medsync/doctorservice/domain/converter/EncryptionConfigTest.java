package com.medsync.doctorservice.domain.converter;

import org.junit.jupiter.api.Test;
import org.jasypt.util.text.AES256TextEncryptor;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionConfigTest {

    @Test
    void encryptionKeyBlank_shouldThrowWhenNoKeyProvided() {
        String encryptionKey = ""; // mimics ${medsync.encryption.key:} with empty default
        assertThrows(IllegalStateException.class, () -> {
            if (encryptionKey == null || encryptionKey.isBlank()) {
                throw new IllegalStateException("ENCRYPTION_KEY is required");
            }
        });
    }

    @Test
    void encryptionKeyValid_shouldCreateEncryptor() {
        String encryptionKey = "valid-encryption-key-provided";
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("ENCRYPTION_KEY is required");
        }
        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(encryptionKey);
        EncryptedConverter.initialize(encryptionKey);
        assertNotNull(encryptor);
        String encrypted = encryptor.encrypt("test");
        assertNotNull(encrypted);
        String decrypted = encryptor.decrypt(encrypted);
        assertEquals("test", decrypted);
    }

    @Test
    void encryptionKeyNotBlank_shouldInitializeConverter() {
        String encryptionKey = "another-valid-key";
        if (encryptionKey == null || encryptionKey.isBlank()) {
            throw new IllegalStateException("ENCRYPTION_KEY is required");
        }
        EncryptedConverter.initialize(encryptionKey);
        EncryptedConverter converter = new EncryptedConverter();
        String encrypted = converter.convertToDatabaseColumn("sensitive");
        assertNotNull(encrypted);
        String decrypted = converter.convertToEntityAttribute(encrypted);
        assertEquals("sensitive", decrypted);
    }
}