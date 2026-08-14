package com.medsync.patientservice.domain.entity;

import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.domain.enums.PatientStatus;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PatientTest {

    @Test
    void shouldCreateValidPatient() {
        Patient patient = createPatient();
        assertAll(
                () -> assertEquals("Abraham", patient.getFirstName()),
                () -> assertEquals("Lara", patient.getLastName()),
                () -> assertEquals("12345678", patient.getDocumentNumber()),
                () -> assertEquals(Gender.MALE, patient.getGender()),
                () -> assertEquals(LocalDate.of(2000, 1, 1), patient.getBirthDate()),
                () -> assertEquals("+573001112233", patient.getPhone()),
                () -> assertEquals("abraham@test.com", patient.getEmail()),
                () -> assertEquals("Barranquilla", patient.getAddress()),
                () -> assertEquals(BloodType.O_POSITIVE, patient.getBloodType()),
                () -> assertEquals(PatientStatus.ACTIVE, patient.getStatus())
        );
    }

    @Test
    void shouldTrimFirstName() {
        Patient patient = createPatient();
        patient.changeFirstName("  Abraham  ");
        assertEquals("Abraham", patient.getFirstName());
    }

    @Test
    void shouldThrowWhenFirstNameIsNull() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeFirstName(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeFirstName("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzCode ReviewCode ReviewABCDEFGHIJKLMN"))
        );
    }

    @Test
    void shouldThrowWhenFirstNameIsBlank() {
        Patient patient = createPatient();
        assertThrows(IllegalArgumentException.class, () -> patient.changeFirstName(" "));
    }

    @Test
    void shouldTrimLastName() {
        Patient patient = createPatient();
        patient.changeLastName("  Rodriguez  ");
        assertEquals("Rodriguez", patient.getLastName());
    }

    @Test
    void shouldThrowWhenLastNameIsBlank() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeLastName("")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeLastName(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeLastName("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyzCode ReviewCode ReviewABCDEFGHIJKLMN"))
        );
    }

    @Test
    void shouldAcceptValidDocumentNumber() {
        Patient patient = createPatient();
        patient.changeDocumentNumber("87654321");
        assertEquals("87654321", patient.getDocumentNumber());
    }

    @Test
    void shouldNormalizePassportDocumentToUpperCase() {
        Patient patient = createPatient();
        patient.changeDocumentNumber("ab123456");
        assertEquals("AB123456", patient.getDocumentNumber());
    }

    @Test
    void shouldThrowWhenDocumentNumberIsInvalid() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeDocumentNumber(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeDocumentNumber("")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeDocumentNumber("1234")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeDocumentNumber("ABC")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeDocumentNumber("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqr"))
        );
    }

    @Test
    void shouldThrowWhenGenderIsNull() {
        Patient patient = createPatient();
        assertThrows(IllegalArgumentException.class, () -> patient.changeGender(null));
    }

    @Test
    void shouldThrowWhenBirthDateIsInvalid() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeBirthDate(LocalDate.now().plusDays(1))),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeBirthDate(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeBirthDate(LocalDate.of(1800, 1, 1)))
        );
    }

    @Test
    void shouldSetValidPhone() {
        Patient patient = createPatient();
        patient.changePhone("+573002223333");
        assertEquals("+573002223333", patient.getPhone());
    }

    @Test
    void shouldThrowWhenPhoneContainsInvalidCharacters() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changePhone(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changePhone("")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changePhone("12345678901234567890123")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changePhone("ABC123"))
        );
    }

    @Test
    void shouldNormalizeEmailToLowerCase() {
        Patient patient = createPatient();
        patient.changeEmail("ABRAHAM@TEST.COM");
        assertEquals("abraham@test.com", patient.getEmail());
    }

    @Test
    void shouldUpdateEmailHashWhenEmailChanges() throws Exception {
        Patient patient = createPatient();
        String previousHash = getField(patient, "emailHash");

        patient.changeEmail("NEW@TEST.COM");

        assertAll(
                () -> assertEquals("new@test.com", patient.getEmail()),
                () -> assertNotEquals(previousHash, getField(patient, "emailHash"))
        );
    }

    @Test
    void shouldThrowWhenEmailFormatIsInvalid() {
        Patient patient = createPatient();
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail(null)),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("a".repeat(121) + "@test.com")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("invalid-email")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("user@domain")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("user@.com")),
                () -> assertThrows(IllegalArgumentException.class, () -> patient.changeEmail("user@domain."))
        );
    }

    @Test
    void shouldChangeAddressToIsInvalid() {
        Patient patient = createPatient();
        patient.changeAddress("");
        assertNull(patient.getAddress());

        patient.changeAddress(null);
        assertNull(patient.getAddress());

        assertThrows(IllegalArgumentException.class, () -> patient.changeAddress("a".repeat(256)));
    }

    @Test
    void shouldSetValidAddress() {
        Patient patient = createPatient();
        patient.changeAddress("Calle 100 #10-20");
        assertEquals("Calle 100 #10-20", patient.getAddress());
    }

    @Test
    void shouldTrimAndNullifyBlankAddress() {
        Patient patient = createPatient();
        patient.changeAddress("   ");
        assertNull(patient.getAddress());

        patient.changeAddress("  Av. 1 #2-3  ");
        assertEquals("Av. 1 #2-3", patient.getAddress());
    }

    @Test
    void shouldThrowWhenBloodTypeIsNull() {
        Patient patient = createPatient();
        assertThrows(IllegalArgumentException.class, () -> patient.changeBloodType(null));
    }

    @Test
    void shouldThrowWhenStatusIsNull() {
        Patient patient = createPatient();
        assertThrows(IllegalArgumentException.class, () -> patient.changeStatus(null));
    }

    @Test
    void shouldUpdateStatus() {
        Patient patient = createPatient();
        patient.changeStatus(PatientStatus.INACTIVE);
        assertEquals(PatientStatus.INACTIVE, patient.getStatus());
    }

    @Test
    void shouldCreatePatientWithActiveStatus() {
        Patient patient = createPatient();
        assertEquals(PatientStatus.ACTIVE, patient.getStatus());
    }

    @Test
    void shouldUpdateDocumentAndPhoneHashesWhenValuesChange() throws Exception {
        Patient patient = createPatient();
        String documentHash = getField(patient, "documentNumberHash");
        String phoneHash = getField(patient, "phoneHash");

        patient.changeDocumentNumber("87654321");
        patient.changePhone("+573009998877");

        assertAll(
                () -> assertNotEquals(documentHash, getField(patient, "documentNumberHash")),
                () -> assertNotEquals(phoneHash, getField(patient, "phoneHash"))
        );
    }

    @Test
    void shouldBeEqualWhenIdsAreEqual() throws Exception {
        UUID id = UUID.randomUUID();
        Patient patient1 = createPatient();
        Patient patient2 = createPatient();
        setId(patient1, id);
        setId(patient2, id);
        assertEquals(patient1, patient2);
    }

    @Test
    void shouldNotBeEqualWhenIdsAreDifferent() throws Exception {
        Patient patient1 = createPatient();
        Patient patient2 = createPatient();
        setId(patient1, UUID.randomUUID());
        setId(patient2, UUID.randomUUID());
        assertNotEquals(patient1, patient2);
    }

    @Test
    void shouldReturnSameHashCodeForSameClass() {
        Patient patient1 = createPatient();
        Patient patient2 = createPatient();
        assertEquals(patient1.hashCode(), patient2.hashCode());
    }

    @Test
    void equals_should_follow_identity_rules() {
        Patient p = createPatient();

        assertFalse(p.equals(null));
        assertFalse(p.equals(new Object()));
    }

    @Test
    void equals_should_use_id_for_comparison() throws Exception {
        Patient p1 = createPatient();
        Patient p2 = createPatient();

        UUID id = UUID.randomUUID();
        setId(p1, id);
        setId(p2, id);

        assertEquals(p1, p2);

        setId(p2, UUID.randomUUID());
        assertNotEquals(p1, p2);
    }

    @Test
    void equals_should_return_false_when_id_is_null() {
        Patient p1 = createPatient();
        Patient p2 = createPatient();

        // ambos id = null (nunca persistidos)
        assertNotEquals(p1, p2);
    }

    private Patient createPatient() {
        return Patient.create(
                "Abraham",
                "Lara",
                "12345678",
                Gender.MALE,
                LocalDate.of(2000, 1, 1),
                "+573001112233",
                "abraham@test.com",
                "Barranquilla",
                BloodType.O_POSITIVE
        );
    }

    private void setId(Patient patient, UUID id) throws Exception {
        Field field = Patient.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(patient, id);
    }

    private String getField(Patient patient, String fieldName) throws Exception {
        Field field = Patient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(patient);
    }


}