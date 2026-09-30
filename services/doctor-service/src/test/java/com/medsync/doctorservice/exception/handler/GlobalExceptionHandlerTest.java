package com.medsync.doctorservice.exception.handler;

import com.medsync.commoncore.error.dto.ProblemDetails;
import com.medsync.commoncore.error.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request("/api/v1/doctors");

    @Test
    @DisplayName("Should malformed JSON return Bad Request")
    void malformedJsonShouldReturnBadRequest() {
        ResponseEntity<ProblemDetails> result = handler.handleMalformedJson(null, request);

        assertProblem(result, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "https://doctor-service/errors/malformed-json");
    }

    @Test
    @DisplayName("Should illegal argument return Invalid Parameter")
    void illegalArgumentShouldReturnInvalidParameter() {
        ResponseEntity<ProblemDetails> result = handler.handleBadRequest(
                new IllegalArgumentException("Invalid id"), request
        );

        assertProblem(result, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER,
                "https://doctor-service/errors/invalid-parameter");
        assertEquals("Invalid id", result.getBody().detail());
    }

    @Test
    @DisplayName("Should method argument type mismatch return Invalid Parameter")
    void methodArgumentTypeMismatchShouldReturnInvalidParameter() {
        MethodArgumentTypeMismatchException ex = mock(MethodArgumentTypeMismatchException.class);
        when(ex.getName()).thenReturn("id");
        when(ex.getMessage()).thenReturn("Invalid UUID");

        ResponseEntity<ProblemDetails> result = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(ErrorCode.INVALID_PARAMETER, result.getBody().code());
    }

    @Test
    @DisplayName("Should data integrity violation map email constraint")
    void dataIntegrityViolationShouldMapEmailConstraint() {
        ResponseEntity<ProblemDetails> result = handler.handleDataIntegrityViolation(
                integrity("EMAIL_HASH duplicate key"), request
        );

        assertEquals("A doctor with the same email already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data integrity violation map medical license constraint")
    void dataIntegrityViolationShouldMapMedicalLicenseConstraint() {
        ResponseEntity<ProblemDetails> result = handler.handleDataIntegrityViolation(
                integrity("MEDICAL_LICENSE_HASH duplicate key"), request
        );

        assertEquals("A doctor with the same medical license already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data integrity violation map phone constraint")
    void dataIntegrityViolationShouldMapPhoneConstraint() {
        ResponseEntity<ProblemDetails> result = handler.handleDataIntegrityViolation(
                integrity("PHONE_HASH duplicate key"), request
        );

        assertEquals("A doctor with the same phone number already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data integrity violation use generic detail when constraint is unknown")
    void dataIntegrityViolationShouldUseGenericDetailWhenConstraintIsUnknown() {
        ResponseEntity<ProblemDetails> result = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("db", new RuntimeException("unknown_constraint")),
                request
        );

        assertProblem(result, HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESOURCE,
                "https://doctor-service/errors/database-constraint");
        assertEquals(
                "The operation could not be completed because it violates a database constraint.",
                result.getBody().detail()
        );
    }

    @Test
    @DisplayName("Should optimistic locking failure return Conflict")
    void optimisticLockingFailureShouldReturnConflict() {
        ResponseEntity<ProblemDetails> result = handler.handleOptimisticLockingFailure(
                new OptimisticLockingFailureException("version"), request
        );

        assertProblem(result, HttpStatus.CONFLICT, ErrorCode.CONFLICT,
                "https://doctor-service/errors/concurrent-update");
    }

    private DataIntegrityViolationException integrity(String message) {
        return new DataIntegrityViolationException("db", new RuntimeException(message));
    }

    private HttpServletRequest request(String uri) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(uri);
        return request;
    }

    private void assertProblem(
            ResponseEntity<ProblemDetails> response,
            HttpStatus status,
            ErrorCode code,
            String type
    ) {
        assertAll(
                () -> assertEquals(status, response.getStatusCode()),
                () -> assertNotNull(response.getBody()),
                () -> assertEquals(status.value(), response.getBody().status()),
                () -> assertEquals(code, response.getBody().code()),
                () -> assertEquals(type, response.getBody().type()),
                () -> assertEquals("/api/v1/doctors", response.getBody().instance())
        );
    }
}
