package com.bankflow.service;

import com.bankflow.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private void sendEmail(String to, String subject, String body) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);

        mailSender.send(message);
    }

    public void sendVerificationEmail(User user, String token) {

        String verificationUrl = frontendUrl + "/verify-email?token=" + token;

        String subject = "Verify your BankFlow Account";

        String body = """
                Dear %s,
                
                Welcome to BankFlow!
                
                Thank you for creating your account.
                
                To activate your account, please verify your email address by clicking the link below:
                
                %s
                
                This verification link is valid for 24 hours.
                
                If you did not create a BankFlow account, please ignore this email.
                
                Regards,
                BankFlow Team
                """.formatted(user.getFullName(), verificationUrl);

        sendEmail(user.getEmail(), subject, body);
    }
}