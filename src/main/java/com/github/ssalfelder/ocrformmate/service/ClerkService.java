package com.github.ssalfelder.ocrformmate.service;

import com.github.ssalfelder.ocrformmate.model.Clerk;
import com.github.ssalfelder.ocrformmate.repository.ClerkRepository;
import org.springframework.stereotype.Service;

@Service
public class ClerkService {

    private final ClerkRepository clerkRepository;

    public ClerkService(ClerkRepository clerkRepository) {
        this.clerkRepository = clerkRepository;
    }

    public boolean isSecretValid(Integer clerkId, String inputSecret) {
        return clerkRepository.findById(clerkId)
                .map(clerk -> clerk.getSecret().equals(inputSecret))
                .orElse(false);
    }

    public Clerk findBySecret(String secret) {
        return clerkRepository.findBySecret(secret)
                .orElseThrow(() -> new RuntimeException("Kein Clerk mit diesem Secret"));
    }
}
