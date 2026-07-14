package com.justeat.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String username, String otp, int expiryMinutes) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject("JustEat – Password Reset OTP");
            helper.setText(buildEmailBody(username, otp, expiryMinutes), true);

            mailSender.send(message);
            logger.info("OTP email sent to: {}", toEmail);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException(
                    "Failed to send OTP email. Please verify SMTP settings (username/app password) and try again.");
        }
    }

    private String buildEmailBody(String username, String otp, int expiryMinutes) {
        return "<div style='font-family:Arial,sans-serif;max-width:480px;margin:auto;padding:32px;border:1px solid #eee;border-radius:8px'>"
                + "<h2 style='color:#e63946'>JustEat Password Reset</h2>"
                + "<p>Hi <strong>" + username + "</strong>,</p>"
                + "<p>You requested a password reset. Use the OTP below to set a new password.</p>"
                + "<div style='text-align:center;margin:24px 0'>"
                + "<span style='font-size:36px;font-weight:bold;letter-spacing:8px;color:#e63946'>" + otp + "</span>"
                + "</div>"
                + "<p>This OTP expires in <strong>" + expiryMinutes + " minutes</strong>.</p>"
                + "<p style='color:#999;font-size:12px'>If you did not request this, please ignore this email.</p>"
                + "</div>";
    }
}
