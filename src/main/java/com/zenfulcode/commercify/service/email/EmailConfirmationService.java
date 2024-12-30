package com.zenfulcode.commercify.service.email;

import com.zenfulcode.commercify.domain.model.ConfirmationToken;
import com.zenfulcode.commercify.domain.model.User;
import com.zenfulcode.commercify.repository.ConfirmationTokenRepository;
import com.zenfulcode.commercify.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class EmailConfirmationService {
    private final ConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional
    public void createConfirmationToken(User user) {
        // Delete any existing unconfirmed tokens
        tokenRepository.findByUserIdAndConfirmedFalse(user.getId())
                .ifPresent(tokenRepository::delete);

        // Create new token
        ConfirmationToken token = ConfirmationToken.builder()
                .user(user)
                .confirmed(false)
                .build();

        ConfirmationToken savedToken = tokenRepository.save(token);

        try {
            emailService.sendConfirmationEmail(user.getEmail(), savedToken.getToken());
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    @Transactional
    public boolean confirmEmail(String token) {
        ConfirmationToken confirmationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid confirmation token"));

        if (confirmationToken.isConfirmed()) {
            throw new RuntimeException("Email already confirmed");
        }

        if (Instant.now().isAfter(confirmationToken.getExpiryDate())) {
            throw new RuntimeException("Confirmation token expired");
        }

        confirmationToken.setConfirmed(true);
        User user = confirmationToken.getUser();
        user.setEmailConfirmed(true);

        tokenRepository.save(confirmationToken);
        userRepository.save(user);

        return true;
    }

    @Transactional
    public void resendConfirmationEmail(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEnabled()) {
            throw new RuntimeException("Email already confirmed");
        }

        createConfirmationToken(user);
    }
}