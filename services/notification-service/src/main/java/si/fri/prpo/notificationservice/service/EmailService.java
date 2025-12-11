package si.fri.prpo.notificationservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import si.fri.prpo.notificationservice.exception.NotificationException;

import jakarta.mail.internet.MimeMessage;

@Service
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final boolean mockMode;
    private final String fromEmail;
    private final String fromName;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${notification.mock-mode:true}") boolean mockMode,
            @Value("${notification.email.from:noreply@bookingsystem.com}") String fromEmail,
            @Value("${notification.email.from-name:Booking System}") String fromName) {
        this.mailSender = mailSender;
        this.mockMode = mockMode;
        this.fromEmail = fromEmail;
        this.fromName = fromName;

        if (mockMode) {
            log.info("Email Service initialized in MOCK mode");
        } else {
            log.info("Email Service initialized in REAL mode");
        }
    }

    public void sendSimpleEmail(String to, String subject, String content) {
        if (mockMode) {
            sendMockEmail(to, subject, content);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            log.info("Email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", to, e);
            // Fallback to mock mode if email fails
            log.warn("Falling back to MOCK mode due to email error");
            sendMockEmail(to, subject, content);
        }
    }

    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        if (mockMode) {
            sendMockEmail(to, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to: {}", to, e);
            // Fallback to mock mode if email fails
            log.warn("Falling back to MOCK mode due to email error");
            sendMockEmail(to, subject, htmlContent);
        }
    }

    private void sendMockEmail(String to, String subject, String content) {
        log.info("MOCK: Sending email");
        log.info("  To: {}", to);
        log.info("  Subject: {}", subject);
        log.info("  Content: {}", content.substring(0, Math.min(100, content.length())) + "...");
        log.info("MOCK: Email sent successfully");
    }

    public boolean isMockMode() {
        return mockMode;
    }
}
