package com.sal.unipile;

import com.sal.user.User;
import com.sal.user.UserRepository;
import com.sal.user.dto.AuthRequest;
import com.sal.user.dto.AuthResponse;
import com.sal.unipile.persistence.Account;
import com.sal.unipile.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AuthResponse register(AuthRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail().toLowerCase());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("E-mail já cadastrado");
        }

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(request.getEmail().toLowerCase())
                .encryptedPassword(passwordEncoder.encode(request.getPassword()))
                .build();
        
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
            throw new IllegalArgumentException("Usuário ou senha inválidos");
        }

        User user = userOpt.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getEncryptedPassword())) {
            throw new IllegalArgumentException("Usuário ou senha inválidos");
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

    private String generateToken(UUID userId) {
        return userId.toString() + ":" + UUID.randomUUID().toString();
    }
}