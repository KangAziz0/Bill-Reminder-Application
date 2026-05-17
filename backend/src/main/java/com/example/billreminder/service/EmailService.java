package com.example.billreminder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp, String purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        if (mailSender instanceof JavaMailSenderImpl sender && sender.getUsername() != null) {
            message.setFrom(sender.getUsername());
        }
        message.setTo(to);
        message.setSubject("Kode OTP untuk " + purpose);
        message.setText("Kode OTP Anda: " + otp + "\nBerlaku selama 5 menit.");
        mailSender.send(message);
    }
}
