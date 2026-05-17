package com.example.billreminder.service;

import com.example.billreminder.config.JwtUtil;
import com.example.billreminder.dto.AuthResponse;
import com.example.billreminder.dto.LoginRequest;
import com.example.billreminder.dto.RegisterRequest;
import com.example.billreminder.entity.User;
import com.example.billreminder.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OtpService otpService;
    private final EmailService emailService;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        if (request.getOtp() == null || request.getOtp().isBlank()) {
            String otp = otpService.generateOtpWithRateLimit("register", request.getEmail(), false);
            emailService.sendOtpEmail(request.getEmail(), otp, "registrasi akun");
            throw new RuntimeException("OTP_REQUIRED: OTP telah dikirim ke email. Masukkan OTP untuk melanjutkan registrasi.");
        }

        if (!otpService.verifyOtp("register", request.getEmail(), request.getOtp())) {
            throw new RuntimeException("OTP tidak valid atau kedaluwarsa.");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phoneNumber(request.getPhoneNumber())
                .build();

        userRepository.save(user);
        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (request.getOtp() == null || request.getOtp().isBlank()) {
            String otp = otpService.generateOtpWithRateLimit("login", request.getEmail(), false);
            emailService.sendOtpEmail(request.getEmail(), otp, "login akun");
            throw new RuntimeException("OTP_REQUIRED: OTP telah dikirim ke email. Masukkan OTP untuk melanjutkan login.");
        }

        if (!otpService.verifyOtp("login", request.getEmail(), request.getOtp())) {
            throw new RuntimeException("OTP tidak valid atau kedaluwarsa.");
        }

        String token = jwtUtil.generateToken(user);
        return buildAuthResponse(user, token);
    }

    public void resendOtp(String purpose, String email) {
        String normalizedPurpose = "register".equalsIgnoreCase(purpose) ? "register" : "login";
        String otp = otpService.generateOtpWithRateLimit(normalizedPurpose, email, true);
        String label = "register".equals(normalizedPurpose) ? "registrasi akun" : "login akun";
        emailService.sendOtpEmail(email, otp, label);
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    public User getCurrentUserFromEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
