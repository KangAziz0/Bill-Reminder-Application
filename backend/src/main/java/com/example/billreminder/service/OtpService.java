package com.example.billreminder.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final long OTP_TTL_SECONDS = 300;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final Map<String, Instant> resendLimitStore = new ConcurrentHashMap<>();
    private static final long RESEND_COOLDOWN_SECONDS = 60;

    @Value("${app.mfa.otp-digits:6}")
    private int otpDigits;

    public String generateOtp(String purpose, String email) {
        int digits = Math.max(4, otpDigits);
        int bound = (int) Math.pow(10, digits);
        int value = secureRandom.nextInt(bound);
        String otp = String.format("%0" + digits + "d", value);
        otpStore.put(key(purpose, email), new OtpEntry(otp, Instant.now().plusSeconds(OTP_TTL_SECONDS)));
        return otp;
    }

    public String generateOtpWithRateLimit(String purpose, String email, boolean forceResend) {
        String key = key(purpose, email);
        Instant nextAllowed = resendLimitStore.get(key);
        Instant now = Instant.now();
        if (forceResend && nextAllowed != null && now.isBefore(nextAllowed)) {
            long waitSeconds = nextAllowed.getEpochSecond() - now.getEpochSecond();
            throw new RuntimeException("Terlalu sering meminta OTP. Coba lagi dalam " + waitSeconds + " detik.");
        }
        String otp = generateOtp(purpose, email);
        resendLimitStore.put(key, now.plusSeconds(RESEND_COOLDOWN_SECONDS));
        return otp;
    }

    public boolean verifyOtp(String purpose, String email, String otp) {
        OtpEntry entry = otpStore.get(key(purpose, email));
        if (entry == null || Instant.now().isAfter(entry.expiresAt()) || otp == null) {
            return false;
        }
        boolean valid = entry.code().equals(otp.trim());
        if (valid) {
            otpStore.remove(key(purpose, email));
        }
        return valid;
    }

    private String key(String purpose, String email) {
        return purpose + ":" + email.toLowerCase();
    }

    private record OtpEntry(String code, Instant expiresAt) {}
}
