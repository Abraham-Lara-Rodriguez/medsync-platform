package com.medsync.authservice.service.auth;

import com.medsync.authservice.dto.auth.request.AuthRequest;
import com.medsync.authservice.dto.auth.response.AuthResponse;

public interface AuthService {
    AuthResponse login(AuthRequest request);

    AuthResponse refreshToken(String refreshToken);
}
