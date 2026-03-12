package com.collab.auth.service;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import com.collab.auth.web.dto.RegisterRequest;
import com.collab.auth.web.dto.RegisterResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileClient userProfileClient;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       UserProfileClient userProfileClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userProfileClient = userProfileClient;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new UserAlreadyExistsException(normalizedEmail);
        }
        String hash = passwordEncoder.encode(request.password());
        User user = new User(UUID.randomUUID(), normalizedEmail, hash, OffsetDateTime.now());
        userRepository.save(user);
        // best-effort create profile in User Service; we don't fail register if profile call fails
        try {
            userProfileClient.createProfile(user.getId(), user.getEmail());
        } catch (Exception ignored) {
            // log could be added later
        }
        return new RegisterResponse(user.getId(), user.getEmail(), user.getCreatedAt());
    }
}
