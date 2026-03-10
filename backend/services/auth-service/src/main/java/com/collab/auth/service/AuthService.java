package com.collab.auth.service;

import com.collab.auth.domain.User;
import com.collab.auth.domain.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResult login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException());

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateAccessToken(user);
        return new LoginResult(token, jwtService);
    }

    public static class LoginResult {
        private final String accessToken;
        private final JwtService jwtService;

        LoginResult(String accessToken, JwtService jwtService) {
            this.accessToken = accessToken;
            this.jwtService = jwtService;
        }

        public String accessToken() {
            return accessToken;
        }
    }
}
