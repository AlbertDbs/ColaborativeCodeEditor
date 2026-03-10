package com.collab.auth.service;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class GoogleLoginService {

    private final GoogleTokenVerifier tokenVerifier;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public GoogleLoginService(GoogleTokenVerifier tokenVerifier,
                              UserRepository userRepository,
                              PasswordEncoder passwordEncoder,
                              JwtService jwtService) {
        this.tokenVerifier = tokenVerifier;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public String loginWithIdToken(String idToken) {
        Optional<GoogleTokenVerifier.GoogleTokenPayload> payloadOpt = tokenVerifier.verify(idToken);
        if (payloadOpt.isEmpty() || !payloadOpt.get().emailVerified()) {
            throw new InvalidCredentialsException();
        }
        String email = payloadOpt.get().email().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createGoogleUser(email));

        return jwtService.generateAccessToken(user);
    }

    private User createGoogleUser(String email) {
        // Store a placeholder password hash since column is NOT NULL.
        String placeholderHash = passwordEncoder.encode("GOOGLE_LOGIN_PLACEHOLDER");
        User user = new User(UUID.randomUUID(), email, placeholderHash, OffsetDateTime.now());
        return userRepository.save(user);
    }
}
