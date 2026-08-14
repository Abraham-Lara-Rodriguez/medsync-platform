package com.medsync.patientservice.domain.constants;

import java.util.Locale;

public final class PatientConstraints {
    private PatientConstraints() {
    }

    public static final int MAX_NAME_LENGTH = 80;
    public static final int MAX_DOCUMENT_LENGTH = 20;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final int MAX_EMAIL_LENGTH = 120;
    public static final int MAX_ADDRESS_LENGTH = 255;
    public static final int MAX_HUMAN_LIFETIME_YEARS = 150;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_REGEX = "^[0-9+\\-() ]+$";
    public static final String PASSPORT_REGEX = "^[A-Z0-9]{6,9}$";

    public static String normalizeDocument(String documentNumber) {
        return documentNumber.trim().toUpperCase(Locale.ROOT);
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizePhone(String phone) {
        return phone.trim();
    }
}
