package com.collab.user.service;

import com.collab.user.domain.UserProfile;
import com.collab.user.domain.UserProfileRepository;
import com.collab.user.web.dto.UpdateDisplayNameRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class UserProfileService {

    private final UserProfileRepository repository;

    public UserProfileService(UserProfileRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public UserProfile findById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new UserProfileNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public UserProfile findByEmail(String email) {
        return repository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UserProfileNotFoundException(email));
    }

    @Transactional
    public UserProfile upsert(UUID id, String email) {
        return repository.findById(id).orElseGet(() -> {
            OffsetDateTime now = OffsetDateTime.now();
            UserProfile profile = new UserProfile(id, email.toLowerCase(), null, now, now);
            return repository.save(profile);
        });
    }

    @Transactional
    public UserProfile updateDisplayName(UUID id, UpdateDisplayNameRequest request) {
        UserProfile profile = findById(id);
        profile.setDisplayName(request.displayName());
        return repository.save(profile);
    }
}
