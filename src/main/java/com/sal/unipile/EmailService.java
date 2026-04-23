package com.sal.unipile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:3000}")
    private String baseUrl;

    @Async
    public void sendPasswordResetEmail(String email, String token) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("Recuperação de senha - SAL");
            message.setText(String.format(
                    "Você solicitou recuperação de senha.\n\n" +
                    "Clique no link abaixo para criar uma nova senha:\n" +
                    "%s/reset-password?token=%s\n\n" +
                    "Este link expira em 1 hora.\n\n" +
                    "Se você não solicitou isso, ignore este email.",
                    baseUrl, token
            ));

            mailSender.send(message);
            log.info("Password reset email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send password reset email to {}: {}", email, e.getMessage());
        }
    }
}