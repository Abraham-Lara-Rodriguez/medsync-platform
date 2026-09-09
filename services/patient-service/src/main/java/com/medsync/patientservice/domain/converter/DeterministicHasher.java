package com.medsync.patientservice.domain.converter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class DeterministicHasher {

    private static final String ALGORITHM = "HmacSHA256";
    private static SecretKeySpec KEY;

    private DeterministicHasher() {
    }

    public static void initialize(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Hash key must not be null or blank");
        }
        KEY = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    public static String hash(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        if (KEY == null) {
            throw new IllegalStateException("DeterministicHasher has not been initialized");
        }
        try {
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(KEY);
            byte[] result = mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(result);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("Unable to compute hash", e);
        }
    }
}