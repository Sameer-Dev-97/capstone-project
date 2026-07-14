package com.justeat.service;

import com.justeat.dto.*;
import com.justeat.entity.PasswordResetToken;
import com.justeat.entity.Role;
import com.justeat.entity.User;
import com.justeat.repository.PasswordResetTokenRepository;
import com.justeat.repository.UserRepository;
import com.justeat.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${otp.expiry-minutes:10}")
    private int otpExpiryMinutes;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new user: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .role(Role.valueOf(request.getRole()))
                .build();

        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Registration successful")
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("User login attempt: {}", request.getUsername());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user);

        logger.info("User logged in successfully: {}", user.getUsername());

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .message("Login successful")
                .build();
    }

    @Transactional
    public String initiatePasswordReset(PasswordResetRequest request) {
        logger.info("Password reset initiated for email: {}", request.getEmail());

        // Look up user by email (not username)
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("No account found with that email address"));

        // Invalidate any existing tokens for this user
        passwordResetTokenRepository.deleteByUser(user);

        // Generate a cryptographically secure 6-digit OTP
        String otp = String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(otp)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .used(false)
                .build();

        passwordResetTokenRepository.save(resetToken);

        // Send OTP via email
        emailService.sendOtpEmail(user.getEmail(), user.getUsername(), otp, otpExpiryMinutes);

        logger.info("OTP sent to email for user: {}", user.getUsername());
        return "OTP sent to your email address. It expires in " + otpExpiryMinutes + " minutes.";
    }

    @Transactional
    public String confirmPasswordReset(PasswordResetConfirmRequest request) {
        logger.info("Password reset confirmation attempt");

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new RuntimeException("Invalid OTP. Please request a new one."));

        if (resetToken.getUsed()) {
            throw new RuntimeException("This OTP has already been used.");
        }

        if (resetToken.isExpired()) {
            throw new RuntimeException("OTP has expired. Please request a new one.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        logger.info("Password reset successful for user: {}", user.getUsername());
        return "Password reset successful. You can now log in with your new password.";
    }

}
