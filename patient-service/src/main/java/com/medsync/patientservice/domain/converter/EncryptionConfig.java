package com.medsync.patientservice.domain.converter;

import org.jasypt.util.text.AES256TextEncryptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptionConfig {

    @Value("${medsync.encryption.key:}")
    private String encryptionKey;

    @Bean
    public AES256TextEncryptor aes256TextEncryptor() {
        validateEncryptionKey(encryptionKey);

        AES256TextEncryptor encryptor = new AES256TextEncryptor();
        encryptor.setPassword(encryptionKey);
        EncryptedStringConverter.initialize(encryptionKey);
        return encryptor;
    }

    private void validateEncryptionKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException(
                    "ENCRYPTION_KEY is required to start patient-service"
            );
        }
    }
}