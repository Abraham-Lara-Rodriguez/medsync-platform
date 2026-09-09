package com.medsync.patientservice.domain.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.jasypt.util.text.AES256TextEncryptor;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private static AES256TextEncryptor ENCRYPTOR;

    public static void initialize(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalStateException("Encryption key must not be null or blank");
        }
        
        AES256TextEncryptor newEncryptor = new AES256TextEncryptor();
        newEncryptor.setPassword(key);
        ENCRYPTOR = newEncryptor;
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        if (attribute.isEmpty()) {
            return attribute;
        }
        if (attribute.isBlank()) {
            return null;
        }
        if (ENCRYPTOR == null) {
            throw new IllegalStateException("EncryptedStringConverter has not been initialized");
        }

        return ENCRYPTOR.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        if (dbData.isEmpty()) {
            return dbData;
        }
        if (dbData.isBlank()) {
            return null;
        }
        if (ENCRYPTOR == null) {
            throw new IllegalStateException("EncryptedStringConverter has not been initialized");
        }
        return ENCRYPTOR.decrypt(dbData);
    }
}