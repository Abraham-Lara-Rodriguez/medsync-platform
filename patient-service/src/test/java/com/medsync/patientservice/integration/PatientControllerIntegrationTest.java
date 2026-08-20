package com.medsync.patientservice.integration;

import com.medsync.patientservice.domain.entity.Patient;
import com.medsync.patientservice.domain.enums.BloodType;
import com.medsync.patientservice.domain.enums.Gender;
import com.medsync.patientservice.repository.PatientRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class PatientControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@medsync.test";
    private static final String USER_EMAIL = "user@medsync.test";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PatientRepository patientRepository;

    private UUID patientId;

    @BeforeEach
    void setUp() {
        patientRepository.deleteAll();

        Patient patient = Patient.create(
                "Juan",
                "Pérez",
                "12345678",
                Gender.MALE,
                LocalDate.of(1990, 1, 15),
                "+573001234567",
                "juan@medsync.test",
                "Calle 123",
                BloodType.O_POSITIVE
        );

        patientId = patientRepository.saveAndFlush(patient).getId();
    }

    @Test
    @DisplayName("GET /patients returns persisted patients with USER_READ")
    void getAllPatientsAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_READ")))
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(patientId.toString()))
                .andExpect(jsonPath("$.content[0].email").value("juan@medsync.test"));
    }

    @Test
    @DisplayName("GET /patients without USER_READ returns 403")
    void getAllPatientsForbiddenWithoutAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /patients/{id} returns patient")
    void getPatientById() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{id}", patientId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_READ"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Juan"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("GET /patients/{id} returns 404 when patient does not exist")
    void getPatientByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/patients/{id}", UUID.randomUUID())
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_READ"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.type").value("https://patient-service/errors/resource-not-found"));
    }

    @Test
    @DisplayName("POST /patients creates a patient")
    void createPatient() throws Exception {
        String body = createPatientJson("Ana", "ana@medsync.test", "87654321", "+573009998877");

        mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.containsString("/api/v1/patients/")))
                .andExpect(jsonPath("$.firstName").value("Ana"))
                .andExpect(jsonPath("$.email").value("ana@medsync.test"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /patients rejects duplicate email")
    void createPatientDuplicateEmail() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPatientJson("Other", "juan@medsync.test", "87654321", "+573009998877")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATE_RESOURCE"))
                .andExpect(jsonPath("$.detail").value("Email already exists"));
    }

    @Test
    @DisplayName("POST /patients rejects malformed JSON")
    void createPatientMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://patient-service/errors/malformed-json"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("POST /patients rejects invalid DTO")
    void createPatientValidationError() throws Exception {
        mockMvc.perform(post("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_CREATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstName": "",
                                  "lastName": "",
                                  "documentNumber": "1",
                                  "gender": null,
                                  "birthDate": "2099-01-01",
                                  "phone": "ABC",
                                  "email": "not-an-email",
                                  "bloodType": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.type").value("https://patient-service/errors/validation-error"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.detail").isNotEmpty());
    }

    @Test
    @DisplayName("PUT /patients/{id} updates persisted patient")
    void updatePatient() throws Exception {
        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePatientJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Juan Carlos"))
                .andExpect(jsonPath("$.email").value("juan.updated@medsync.test"))
                .andExpect(jsonPath("$.bloodType").value("A_POSITIVE"));

        Patient updated = patientRepository.findById(patientId).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("Juan Carlos", updated.getFirstName());
        org.junit.jupiter.api.Assertions.assertEquals("juan.updated@medsync.test", updated.getEmail());
    }

    @Test
    @DisplayName("PUT /patients/{id} rejects duplicate document")
    void updatePatientDuplicateDocument() throws Exception {
        Patient second = Patient.create(
                "Ana", "Gómez", "87654321", Gender.FEMALE,
                LocalDate.of(1992, 2, 2), "+573009876543",
                "ana@medsync.test", null, BloodType.A_NEGATIVE
        );
        patientRepository.saveAndFlush(second);

        mockMvc.perform(put("/api/v1/patients/{id}", patientId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_UPDATE")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePatientJson("87654321", "juan.updated2@medsync.test", "+573001111111")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Document number already registered"));
    }

    @Test
    @DisplayName("PATCH /patients/deactivate/{id} deactivates only for ADMIN")
    void deactivatePatientAllowedForAdmin() throws Exception {
        mockMvc.perform(patch("/api/v1/patients/deactivate/{id}", patientId)
                        .header("Authorization", bearerToken(ADMIN_EMAIL, List.of("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertEquals(
                com.medsync.patientservice.domain.enums.PatientStatus.INACTIVE,
                patientRepository.findById(patientId).orElseThrow().getStatus()
        );
    }

    @Test
    @DisplayName("PATCH /patients/deactivate/{id} returns 403 for USER")
    void deactivatePatientForbiddenForUser() throws Exception {
        mockMvc.perform(patch("/api/v1/patients/deactivate/{id}", patientId)
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Protected endpoint without token returns 401")
    void protectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/patients"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Protected endpoint with malformed token returns 401")
    void protectedEndpointWithMalformedToken() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", "Bearer not-a-valid-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Refresh token cannot be used as access token")
    void refreshTokenIsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_READ"), "refresh")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /patients rejects page size above service limit")
    void getAllPatientsRejectsOversizedPage() throws Exception {
        mockMvc.perform(get("/api/v1/patients")
                        .header("Authorization", bearerToken(USER_EMAIL, List.of("ROLE_USER", "USER_READ")))
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_PARAMETER"))
                .andExpect(jsonPath("$.detail").value("Page size must not exceed 100"));
    }

    @Test
    @DisplayName("Actuator health is public")
    void healthIsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private String bearerToken(String subject, List<String> authorities) {
        return bearerToken(subject, authorities, "access");
    }

    private String bearerToken(String subject, List<String> authorities, String type) {
        Instant now = Instant.now();
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_SECRET_BASE64));

        String token = Jwts.builder()
                .issuer("medsync-auth-service")
                .audience().add("medsync-platform").and()
                .subject(subject)
                .issuedAt(Date.from(now))
                .notBefore(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .claim("roles", authorities)
                .signWith(key)
                .compact();

        return "Bearer " + token;
    }

    private String createPatientJson(String firstName, String email, String document, String phone) {
        return """
                {
                  "firstName": "%s",
                  "lastName": "Gómez",
                  "documentNumber": "%s",
                  "gender": "FEMALE",
                  "birthDate": "1992-02-02",
                  "phone": "%s",
                  "email": "%s",
                  "address": "Calle 10",
                  "bloodType": "A_NEGATIVE"
                }
                """.formatted(firstName, document, phone, email);
    }

    private String updatePatientJson() {
        return updatePatientJson("87654321", "juan.updated@medsync.test", "+573001234567");
    }

    private String updatePatientJson(String document, String email, String phone) {
        return """
                {
                  "firstName": "Juan Carlos",
                  "lastName": "Pérez López",
                  "documentNumber": "%s",
                  "gender": "MALE",
                  "birthDate": "1990-01-15",
                  "phone": "%s",
                  "email": "%s",
                  "address": "Carrera 50",
                  "bloodType": "A_POSITIVE"
                }
                """.formatted(document, phone, email);
    }
}
