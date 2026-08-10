package com.bankflow.service;

import com.bankflow.entity.AuditAction;
import com.bankflow.entity.User;
import com.bankflow.entity.VerificationToken;
import com.bankflow.exception.EmailVerificationException;
import com.bankflow.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;
    private final AuditLogService auditLogService;

    public VerificationToken createVerificationToken(User user) {

        verificationTokenRepository.deleteByUser(user);
        verificationTokenRepository.flush();
        VerificationToken token = VerificationToken.builder()
                .token(UUID.randomUUID().toString())
                .user(user)
                .expiryDate(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        return verificationTokenRepository.save(token);
    }

    @Transactional
    public void verifyEmail(String token) {

        VerificationToken verificationToken =
                verificationTokenRepository.findByToken(token)
                        .orElseThrow(() ->
                                new EmailVerificationException(
                                        "Invalid verification link."
                                ));

        if (verificationToken.isUsed()) {
            throw new EmailVerificationException(
                    "This verification link has already been used."
            );
        }

        if (verificationToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new EmailVerificationException(
                    "This verification link has expired."
            );
        }

        User user = verificationToken.getUser();

        user.setEmailVerified(true);

        auditLogService.log(
                user,
                AuditAction.EMAIL_VERIFIED,
                "Email address verified successfully"
        );

        verificationToken.setUsed(true);

        verificationTokenRepository.save(verificationToken);
    }



}