package com.fortunecloud.resumeai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String from;
    private final boolean enabled;

    public EmailService(JavaMailSender mailSender,
                         @Value("${app.mail.from}") String from,
                         @Value("${app.mail.enabled}") boolean enabled) {
        this.mailSender = mailSender;
        this.from = from;
        this.enabled = enabled;
    }

    public void sendOtp(String toEmail, String fullName, String otp) {
        String subject = "Your verification code";
        String body = "Hi " + fullName + ",\n\n"
                + "Your verification code is: " + otp + "\n"
                + "It expires in 10 minutes.\n\n"
                + "If you didn't request this, you can ignore this email.";
        send(toEmail, subject, body, otp);
    }

    public void sendWelcome(String toEmail, String fullName) {
        String subject = "Welcome, " + fullName + "!";
        String body = "Hi " + fullName + ",\n\n"
                + "Your account is verified and ready to go. You can now build your resume,"
                + " generate cover letters, and manage your profile.\n\n- Resume AI";
        send(toEmail, subject, body, null);
    }

    private void send(String to, String subject, String body, String otpForLog) {
        if (!enabled) {
            // Mail credentials not configured (local/dev). Never block the flow --
            // log the OTP so registration is still testable end-to-end.
            log.warn("[MAIL DISABLED] To: {} | Subject: {} | Body: {}", to, subject, body);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            if (otpForLog != null) {
                log.warn("[MAIL FAILED - OTP FALLBACK] {} -> {}", to, otpForLog);
            }
        }
    }
}
