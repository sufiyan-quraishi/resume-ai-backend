package com.fortunecloud.resumeai.service;

import com.fortunecloud.resumeai.dto.auth.*;
import com.fortunecloud.resumeai.dto.user.UserProfileResponse;
import com.fortunecloud.resumeai.entity.User;
import com.fortunecloud.resumeai.repository.UserRepository;
import com.fortunecloud.resumeai.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        JwtUtil jwtUtil, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        String email = req.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalArgumentException("An account with this email already exists");
        }

        User user = new User();
        user.setFullName(req.getFullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setEmailVerified(false);
        applyNewOtp(user);

        userRepository.save(user);
        emailService.sendOtp(user.getEmail(), user.getFullName(), user.getOtpCode());

        return Map.of(
                "message", "Registered. We've emailed you a 6-digit verification code.",
                "email", user.getEmail());
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));

        if (user.isEmailVerified()) {
            return new AuthResponse(jwtUtil.generateToken(user.getEmail(), user.getId()),
                    new UserProfileResponse(user));
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(req.getOtp().trim())) {
            throw new IllegalArgumentException("Invalid verification code");
        }
        if (user.getOtpExpiresAt() == null || Instant.now().isAfter(user.getOtpExpiresAt())) {
            throw new IllegalArgumentException("This code has expired, please request a new one");
        }

        user.setEmailVerified(true);
        user.setOtpCode(null);
        user.setOtpExpiresAt(null);
        userRepository.save(user);
        emailService.sendWelcome(user.getEmail(), user.getFullName());

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, new UserProfileResponse(user));
    }

    @Transactional
    public Map<String, Object> resendOtp(EmailOnlyRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No account found for this email"));
        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("This account is already verified");
        }
        applyNewOtp(user);
        userRepository.save(user);
        emailService.sendOtp(user.getEmail(), user.getFullName(), user.getOtpCode());
        return Map.of("message", "A new verification code has been emailed to you");
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailIgnoreCase(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getId());
        return new AuthResponse(token, new UserProfileResponse(user));
    }

    private void applyNewOtp(User user) {
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        user.setOtpCode(otp);
        user.setOtpExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));
    }
}
