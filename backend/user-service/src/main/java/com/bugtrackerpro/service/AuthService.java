package com.bugtrackerpro.service;

import com.bugtrackerpro.dto.*;
import com.bugtrackerpro.entity.User;
import com.bugtrackerpro.repository.UserRepository;
import com.bugtrackerpro.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j 
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtUtil jwtUtil;
        private final AuthenticationManager authenticationManager;
        private final OtpService otpService;
        private final EmailService emailService;

        public AuthResponse register(RegisterRequest request) {
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new RuntimeException("Email already exists");
                }

                User user = User.builder()
                                .name(request.getName())
                                .email(request.getEmail())
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(request.getRole())
                                .createdAt(LocalDateTime.now())
                                .build();

                User savedUser = userRepository.save(user);
                String token = jwtUtil.generateToken(
                                new org.springframework.security.core.userdetails.User(
                                        savedUser.getEmail(), savedUser.getPassword(), java.util.Collections.emptyList()),
                                savedUser.getId(),
                                savedUser.getRole());

                return AuthResponse.builder()
                                .token(token)
                                .user(mapToDTO(savedUser))
                                .build();
        }

        public AuthResponse login(LoginRequest request) {
                
                log.info("Inside login method");
                authenticationManager.authenticate(
                                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new RuntimeException("User not found"));

                String token = jwtUtil.generateToken(
                                new org.springframework.security.core.userdetails.User(
                                        user.getEmail(), user.getPassword(), java.util.Collections.emptyList()),
                                user.getId(),
                                user.getRole());

                return AuthResponse.builder()
                                .token(token)
                                .user(mapToDTO(user))
                                .build();
        }

        public void forgotPassword(String email) {
                if (userRepository.findByEmail(email).isEmpty()) {
                        throw new RuntimeException("User not found with email: " + email);
                }
                String otp = otpService.generateOtp(email);
                emailService.sendOtpEmail(email, otp);
        }

        public void verifyOtp(String email, String otp) {
                if (!otpService.validateOtp(email, otp)) {
                        throw new RuntimeException("Invalid or expired OTP");
                }
        }

        public void resetPassword(String email, String newPassword) {
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new RuntimeException("User not found"));
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
        }

        private UserDTO mapToDTO(User user) {
                return UserDTO.builder()
                                .id(user.getId())
                                .name(user.getName())
                                .email(user.getEmail())
                                .role(user.getRole())
                                .createdAt(user.getCreatedAt())
                                .build();
        }
}
