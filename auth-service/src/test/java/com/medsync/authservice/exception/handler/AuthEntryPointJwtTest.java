package com.medsync.authservice.exception.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(MockitoExtension.class)
class AuthEntryPointJwtTest {

    @Mock
    private HttpServletRequest request;

    private final AuthEntryPointJwt entryPoint = new AuthEntryPointJwt(new ObjectMapper().findAndRegisterModules());

    @Test
    @DisplayName("writes a 401 problem-json response with the expected fields")
    void writesUnauthorizedProblemDetail() throws Exception {

        org.mockito.Mockito.when(request.getRequestURI()).thenReturn("/api/v1/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).isEqualTo(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        Map<String, Object> body = new ObjectMapper().findAndRegisterModules().readValue(response.getContentAsByteArray(), new TypeReference<>() {
        });

        Map<String, Object> properties = (Map<String, Object>) body.get("properties");

        assertAll(
                () -> assertThat(body.get("title")).isEqualTo("Unauthorized"),
                () -> assertThat(body.get("detail")).isEqualTo("Authentication is required to access this resource."),
                () -> assertThat(properties.get("path")).isEqualTo("/api/v1/users"),
                () -> assertThat(properties).containsKey("timestamp")
        );
    }
}