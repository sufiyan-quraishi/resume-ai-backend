package com.fortunecloud.resumeai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final String apiKey;
    private final String fromEmail;
    private final String fromName;
    private final boolean enabled;
    private final RestTemplate restTemplate;

    public EmailService(
            @Value("${app.mail.api-key:${MAIL_API_KEY:}}") String apiKey,
            @Value("${app.mail.from:${MAIL_FROM:}}") String fromEmail,
            @Value("${app.mail.from-name:Resume AI Team}") String fromName,
            @Value("${app.mail.enabled:true}") boolean enabled) {
        this.apiKey = apiKey;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.enabled = enabled;
        this.restTemplate = new RestTemplate();
    }

    public void sendOtp(String toEmail, String fullName, String otp) {
        String subject = otp + " is your Resume AI Verification Code";
        String htmlContent = buildOtpHtml(fullName, otp);
        sendViaHttp(toEmail, fullName, subject, htmlContent, otp);
    }

    public void sendWelcome(String toEmail, String fullName) {
        String subject = "Welcome to Resume AI – Supercharge Your Career!";
        String htmlContent = buildWelcomeHtml(fullName);
        sendViaHttp(toEmail, fullName, subject, htmlContent, null);
    }

    private void sendViaHttp(String toEmail, String toName, String subject, String htmlContent, String otpForLog) {
        if (!enabled || apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("[MAIL DISABLED OR NO API KEY] Sending skipped. Fallback OTP for {}: {}", toEmail, otpForLog);
            return;
        }

        try {
            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sender", Map.of("name", fromName, "email", fromEmail));
            payload.put("to", List.of(Map.of("email", toEmail, "name", toName != null ? toName : "User")));
            payload.put("subject", subject);
            payload.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Professional email delivered successfully to {}", toEmail);
            } else {
                throw new RuntimeException("Brevo API responded with: " + response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            if (otpForLog != null) {
                log.warn("[MAIL FAILED - OTP FALLBACK] {} -> {}", toEmail, otpForLog);
            }
        }
    }

    private String buildOtpHtml(String fullName, String otp) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset='UTF-8'>"
                + "<meta name='viewport' content='width=device-width, initial-scale=1.0'>"
                + "<title>Verify Your Identity</title>"
                + "</head>"
                + "<body style='margin:0; padding:0; background-color:#f4f6fb; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f6fb; padding: 40px 0;'>"
                + "    <tr>"
                + "      <td align='center'>"
                + "        <table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.05); max-width:90%;'>"
                + "          <!-- Header Section -->"
                + "          <tr>"
                + "            <td style='background: linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%); padding: 45px 35px; text-align: center; color: #ffffff;'>"
                + "              <h1 style='margin:0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px;'>Resume AI</h1>"
                + "              <p style='margin: 8px 0 0 0; font-size: 14px; opacity: 0.9;'>Empowering Your Career with Intelligent Automation</p>"
                + "            </td>"
                + "          </tr>"
                + "          <!-- Body Content -->"
                + "          <tr>"
                + "            <td style='padding: 40px 35px; color: #1e293b; line-height: 1.6;'>"
                + "              <h2 style='font-size: 20px; font-weight: 700; color: #0f172a; margin-top: 0;'>Hello " + fullName + ",</h2>"
                + "              <p style='font-size: 15px; color: #475569;'>Thank you for choosing <strong>Resume AI</strong>. To ensure the security of your account and complete your registration, please use the One-Time Password (OTP) provided below.</p>"
                + "              <!-- OTP Container Box -->"
                + "              <div style='background: #f8fafc; border: 2px dashed #cbd5e1; border-radius: 12px; text-align: center; padding: 25px 20px; margin: 30px 0;'>"
                + "                <div style='font-size: 12px; font-weight: 600; text-transform: uppercase; color: #64748b; letter-spacing: 1px;'>Verification Code</div>"
                + "                <div style='font-size: 38px; font-weight: 800; letter-spacing: 8px; color: #4f46e5; margin: 10px 0;'>" + otp + "</div>"
                + "                <div style='font-size: 13px; color: #94a3b8;'>Valid for 10 minutes only. Do not share this code.</div>"
                + "              </div>"
                + "              <p style='font-size: 14px; color: #64748b;'>If you did not initiate this request, no action is needed. Your account remains protected, and you can safely disregard this email.</p>"
                + "            </td>"
                + "          </tr>"
                + "          <!-- Footer -->"
                + "          <tr>"
                + "            <td style='background-color: #f8fafc; padding: 25px 35px; border-top: 1px solid #e2e8f0; text-align: center; color: #94a3b8; font-size: 12px;'>"
                + "              <p style='margin: 0;'>&copy; 2026 Resume AI Platform. All rights reserved.</p>"
                + "              <p style='margin: 5px 0 0 0;'>Need help? Contact our support desk at any time.</p>"
                + "            </td>"
                + "          </tr>"
                + "        </table>"
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";
    }

    private String buildWelcomeHtml(String fullName) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='margin:0; padding:0; background-color:#f4f6fb; font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, sans-serif;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f6fb; padding: 40px 0;'>"
                + "    <tr>"
                + "      <td align='center'>"
                + "        <table width='600' cellpadding='0' cellspacing='0' style='background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.05); max-width:90%;'>"
                + "          <tr>"
                + "            <td style='background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 45px 35px; text-align: center; color: #ffffff;'>"
                + "              <h1 style='margin:0; font-size: 28px; font-weight: 800;'>Welcome to Resume AI!</h1>"
                + "              <p style='margin: 8px 0 0 0; font-size: 14px;'>Your journey towards landing your dream role begins here.</p>"
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td style='padding: 40px 35px; color: #1e293b; line-height: 1.7;'>"
                + "              <h2 style='font-size: 20px; font-weight: 700; color: #0f172a;'>Hi " + fullName + ",</h2>"
                + "              <p style='font-size: 15px; color: #475569;'>Your email verification was successful. Your account is fully active and ready to craft standout job application documents.</p>"
                + "              <ul style='color: #334155; font-size: 14px; padding-left: 20px;'>"
                + "                <li><strong>ATS-Optimized Resumes:</strong> Generate role-specific resumes scored for Applicant Tracking Systems.</li>"
                + "                <li><strong>Tailored Cover Letters:</strong> Draft compelling cover letters in seconds.</li>"
                + "                <li><strong>Instant Export:</strong> Download in PDF, Word, or Markdown format with a single click.</li>"
                + "              </ul>"
                + "            </td>"
                + "          </tr>"
                + "          <tr>"
                + "            <td style='background-color: #f8fafc; padding: 25px 35px; border-top: 1px solid #e2e8f0; text-align: center; color: #94a3b8; font-size: 12px;'>"
                + "              &copy; 2026 Resume AI Platform. All rights reserved."
                + "            </td>"
                + "          </tr>"
                + "        </table>"
                + "      </td>"
                + "    </tr>"
                + "  </table>"
                + "</body>"
                + "</html>";
    }
}
