package com.example.billreminder.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Async("otpEmailExecutor")
    public void sendOtpEmail(String to, String otp, String purpose) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            if (mailSender instanceof JavaMailSenderImpl sender && sender.getUsername() != null) {
                helper.setFrom(sender.getUsername());
            }
            helper.setTo(to);
            helper.setSubject("Kode Verifikasi OTP - Bill Reminder");
            helper.setText(buildOtpEmailTemplate(otp, purpose), true);

            mailSender.send(mimeMessage);
            log.info("OTP email sent successfully to {} for purpose: {}", maskEmail(to), purpose);
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to {} for purpose: {}. Error: {}", maskEmail(to), purpose, e.getMessage());
        }
    }

    private String buildOtpEmailTemplate(String otp, String purpose) {
        String purposeLabel = mapPurposeLabel(purpose);
        String[] digits = otp.split("");

        StringBuilder otpBoxes = new StringBuilder();
        for (String digit : digits) {
            otpBoxes.append(String.format(
                "<td style=\"padding:0 4px;\">" +
                "<div style=\"width:44px;height:52px;background:#f7f8fc;border:2px solid #e2e8f0;border-radius:10px;font-size:24px;font-weight:700;color:#2d3748;line-height:52px;text-align:center;\">%s</div>" +
                "</td>", digit));
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8">
              <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0;padding:0;background-color:#f4f6f9;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,'Helvetica Neue',Arial,sans-serif;">
              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#f4f6f9;padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width:480px;background:#ffffff;border-radius:16px;box-shadow:0 4px 24px rgba(0,0,0,0.06);overflow:hidden;">
                      <!-- Header -->
                      <tr>
                        <td style="background:linear-gradient(135deg,#667eea 0%%,#764ba2 100%%);padding:32px 40px;text-align:center;">
                          <h1 style="margin:0;color:#ffffff;font-size:20px;font-weight:700;letter-spacing:-0.3px;">
                            Bill Reminder
                          </h1>
                          <p style="margin:8px 0 0;color:rgba(255,255,255,0.85);font-size:13px;font-weight:400;">
                            Verifikasi Keamanan Akun
                          </p>
                        </td>
                      </tr>
                      <!-- Body -->
                      <tr>
                        <td style="padding:36px 40px 24px;">
                          <p style="margin:0 0 6px;font-size:15px;color:#4a5568;line-height:1.6;">
                            Halo,
                          </p>
                          <p style="margin:0 0 24px;font-size:15px;color:#4a5568;line-height:1.6;">
                            Gunakan kode verifikasi berikut untuk <strong style="color:#2d3748;">%s</strong>:
                          </p>
                          <!-- OTP Code -->
                          <table role="presentation" cellpadding="0" cellspacing="0" style="margin:0 auto 24px;">
                            <tr>
                              %s
                            </tr>
                          </table>
                          <!-- Expiry Notice -->
                          <div style="background:#f7fafc;border:1px solid #e2e8f0;border-radius:10px;padding:14px 18px;margin-bottom:24px;text-align:center;">
                            <p style="margin:0;font-size:13px;color:#718096;">
                              ⏱ Kode ini berlaku selama <strong style="color:#4a5568;">5 menit</strong>
                            </p>
                          </div>
                          <!-- Security Notice -->
                          <div style="border-left:3px solid #e53e3e;padding-left:14px;margin-bottom:0;">
                            <p style="margin:0;font-size:12px;color:#a0aec0;line-height:1.6;">
                              Jangan berikan kode ini kepada siapapun. Tim Bill Reminder tidak akan pernah meminta kode OTP Anda.
                            </p>
                          </div>
                        </td>
                      </tr>
                      <!-- Footer -->
                      <tr>
                        <td style="padding:20px 40px 28px;border-top:1px solid #f0f0f0;text-align:center;">
                          <p style="margin:0;font-size:11px;color:#a0aec0;line-height:1.6;">
                            Email ini dikirim otomatis oleh sistem Bill Reminder.<br>
                            Jika Anda tidak merasa melakukan %s, abaikan email ini.
                          </p>
                        </td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(purposeLabel, otpBoxes.toString(), purposeLabel);
    }

    private String mapPurposeLabel(String purpose) {
        return switch (purpose.toLowerCase()) {
            case "registrasi akun" -> "registrasi akun baru";
            case "login akun" -> "login ke akun Anda";
            default -> purpose;
        };
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return "***";
        String[] parts = email.split("@");
        String name = parts[0];
        String masked = name.length() > 3
            ? name.substring(0, 2) + "***" + name.substring(name.length() - 1)
            : name.substring(0, 1) + "***";
        return masked + "@" + parts[1];
    }
}
