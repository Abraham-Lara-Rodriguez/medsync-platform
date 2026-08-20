package com.medsync.patientservice.exception.handler;

import com.medsync.commoncore.error.enums.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private final HttpServletRequest request = request("/api/v1/patients");

    @Test
    @DisplayName("Should malformed Json Should Return Bad Request")
    void malformedJsonShouldReturnBadRequest() {
        var result = handler.handleMalformedJson(null, request);

        assertProblem(result, HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR,
                "https://patient-service/errors/malformed-json");
    }

    @Test
    @DisplayName("Should illegal Argument Should Return Invalid Parameter")
    void illegalArgumentShouldReturnInvalidParameter() {
        var result = handler.handleBadRequest(
                new IllegalArgumentException("Invalid id"), request
        );

        assertProblem(result, HttpStatus.BAD_REQUEST, ErrorCode.INVALID_PARAMETER,
                "https://patient-service/errors/invalid-parameter");
        assertEquals("Invalid id", result.getBody().detail());
    }

    @Test
    @DisplayName("Should method Argument Type Mismatch Should Return Invalid Parameter")
    void methodArgumentTypeMismatchShouldReturnInvalidParameter() {
        var ex = mock(MethodArgumentTypeMismatchException.class);
        whenMessage(ex, "id", "Invalid UUID");

        var result = handler.handleBadRequest(ex, request);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
        assertEquals(ErrorCode.INVALID_PARAMETER, result.getBody().code());
    }

    @Test
    @DisplayName("Should invalid Token Should Return Unauthorized")
    void invalidTokenShouldReturnUnauthorized() {
        var result = handler.handleInvalidToken(
                new InvalidTokenException("expired"), request
        );

        assertProblem(result, HttpStatus.UNAUTHORIZED, ErrorCode.UNAUTHORIZED,
                "https://patient-service/errors/invalid-token");
    }

    @Test
    @DisplayName("Should data Integrity Violation Should Use Generic Detail When Constraint Is Unknown")
    void dataIntegrityViolationShouldUseGenericDetailWhenConstraintIsUnknown() {
        var result = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("db", new RuntimeException("some_constraint")),
                request
        );

        assertProblem(result, HttpStatus.CONFLICT, ErrorCode.DUPLICATE_RESOURCE,
                "https://patient-service/errors/database-constraint");
        assertEquals(
                "The operation could not be completed because it violates a database constraint.",
                result.getBody().detail()
        );
    }

    @Test
    @DisplayName("Should data Integrity Violation Should Map Email Constraint")
    void dataIntegrityViolationShouldMapEmailConstraint() {
        var result = handler.handleDataIntegrityViolation(
                integrity("EMAIL_HASH duplicate key"), request
        );

        assertEquals("A patient with the same email already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data Integrity Violation Should Map Document Constraint")
    void dataIntegrityViolationShouldMapDocumentConstraint() {
        var result = handler.handleDataIntegrityViolation(
                integrity("DOCUMENT_NUMBER_HASH duplicate key"), request
        );

        assertEquals("A patient with the same document number already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data Integrity Violation Should Map Phone Constraint")
    void dataIntegrityViolationShouldMapPhoneConstraint() {
        var result = handler.handleDataIntegrityViolation(
                integrity("PHONE_HASH duplicate key"), request
        );

        assertEquals("A patient with the same phone number already exists.", result.getBody().detail());
    }

    @Test
    @DisplayName("Should data Integrity Violation Should Handle Null Cause Message")
    void dataIntegrityViolationShouldHandleNullCauseMessage() {
        var result = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("db", new RuntimeException((String) null)),
                request
        );

        assertEquals(
                "The operation could not be completed because it violates a database constraint.",
                result.getBody().detail()
        );
    }

    @Test
    @DisplayName("Should optimistic Locking Failure Should Return Conflict")
    void optimisticLockingFailureShouldReturnConflict() {
        var result = handler.handleOptimisticLockingFailure(
                new org.springframework.dao.OptimisticLockingFailureException("version"), request
        );

        assertProblem(result, HttpStatus.CONFLICT, ErrorCode.CONFLICT,
                "https://patient-service/errors/concurrent-update");
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
            org.springframework.http.ResponseEntity<com.medsync.commoncore.error.dto.ProblemDetails> response,
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
                () -> assertEquals("/api/v1/patients", response.getBody().instance())
        );
    }

    private void whenMessage(MethodArgumentTypeMismatchException ex, String name, String message) {
        org.mockito.Mockito.when(ex.getName()).thenReturn(name);
        org.mockito.Mockito.when(ex.getMessage()).thenReturn(message);
    }
}
