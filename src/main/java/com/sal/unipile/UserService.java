package com.sal.unipile;

import com.sal.user.User;
import com.sal.user.UserRepository;
import com.sal.user.dto.AuthRequest;
import com.sal.user.dto.AuthResponse;
import com.sal.unipile.persistence.Account;
import com.sal.unipile.persistence.AccountRepository;
import com.sal.unipile.persistence.PasswordResetToken;
import com.sal.unipile.persistence.PasswordResetTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(AuthRequest request) {
        User user;
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail().toLowerCase());
        if (existingUser.isPresent()) {
            user = existingUser.get();
            user.setEncryptedPassword(passwordEncoder.encode(request.getPassword()));
        } else {
            user = User.builder()
                    .id(UUID.randomUUID())
                    .email(request.getEmail().toLowerCase())
                    .encryptedPassword(passwordEncoder.encode(request.getPassword()))
                    .build();
        }
        user = userRepository.save(user);

        String token = generateToken(user.getId());

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail().toLowerCase());
        
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getEncryptedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuário ou senha inválidos");
        }

        String token = generateToken(user.getId());

        return AuthResponse.builder()
                .userId(user.getId().toString())
                .email(user.getEmail())
                .token(token)
                .build();
    }

    public Optional<Account> findByUserId(String userId) {
        try {
            UUID uuid = UUID.fromString(userId);
            return accountRepository.findByUserId(uuid).stream().findFirst();
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public void changePassword(String userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getEncryptedPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Senha atual incorreta");
        }

        user.setEncryptedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        passwordResetTokenRepository.findByUserIdAndUsedAtIsNullAndExpiresAtAfter(user.getId(), Instant.now())
                .ifPresent(t -> {
                    t.setUsedAt(Instant.now());
                    passwordResetTokenRepository.save(t);
                });

        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(1, ChronoUnit.HOURS);

        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .userId(user.getId())
                .expiresAt(expiresAt)
                .build();

        passwordResetTokenRepository.save(resetToken);
        emailService.sendPasswordResetEmail(email, token);

        log.info("Password reset token created for user: {}", user.getId());
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token já utilizado");
        }

        if (resetToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expirado");
        }

        User user = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));

        user.setEncryptedPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetToken.setUsedAt(Instant.now());
        passwordResetTokenRepository.save(resetToken);

        log.info("Password reset successful for user: {}", user.getId());
    }

    private String generateToken(UUID userId) {
        return userId.toString() + ":" + UUID.randomUUID().toString();
    }
}
