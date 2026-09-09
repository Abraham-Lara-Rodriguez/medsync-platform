package com.medsync.doctorservice.domain.constants;

import java.util.Locale;

public final class DoctorConstraints {
    private DoctorConstraints() {
    }

    public static final int MAX_NAME_LENGTH = 100;
    public static final int MAX_MEDICAL_LICENSE_LENGTH = 50;
    public static final int MAX_EMAIL_LENGTH = 100;
    public static final int MAX_PHONE_LENGTH = 20;
    public static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    public static final String PHONE_REGEX = "^[0-9+\\-() ]+$";
    public static final String MEDICAL_LICENSE_REGEX = "^[A-Za-z0-9\\-]+$";

    public static String normalizeName(String name) {
        return name.trim();
    }

    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    public static String normalizePhone(String phone) {
        return phone.trim();
    }

    public static String normalizeMedicalLicense(String license) {
        return license.trim().toUpperCase(Locale.ROOT);
    }
}