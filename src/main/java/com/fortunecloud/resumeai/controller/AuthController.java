package com.fortunecloud.resumeai.controller;

import com.fortunecloud.resumeai.dto.auth.*;
import com.fortunecloud.resumeai.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * NEW. Registration, email OTP verification, and login. Kept separate from
 * ResumeController so the existing resume/cover-letter/export API is
 * untouched.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/verify-otp")
    public AuthResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return authService.verifyOtp(request);
    }

    @PostMapping("/resend-otp")
    public Map<String, Object> resendOtp(@Valid @RequestBody EmailOnlyRequest request) {
        return authService.resendOtp(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
